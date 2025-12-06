package com.june.service;

import com.june.config.ApiKeysConfig;
import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PaywallEnterpriseApp {

    private final AtomicInteger stripeKeyIndex = new AtomicInteger(0);

    // This bean correctly initializes ONE Stripe key for the entire application on startup.
    @Bean
    ApplicationRunner bootPaywall(
        ApiKeysConfig apiKeysConfig,
        @Value("${app.vaultDir:./vault}") String vaultDir,
        @Value("${app.domain:https://example.com}") String domain,
        @Value("${app.owner:Sanel Crnkic}") String owner) {
        return args -> {
            List<String> stripeKeys = apiKeysConfig.getStripe().getSecretKeys();
            Files.createDirectories(Path.of(vaultDir));
            if (stripeKeys != null && !stripeKeys.isEmpty()) {
                // Rotation logic: pick the next key from the list on each application start.
                int index = stripeKeyIndex.getAndIncrement() % stripeKeys.size();
                String stripeKey = stripeKeys.stream().filter(k -> k != null && !k.isBlank()).toList().get(index);
                if (stripeKey != null) {
                    Stripe.apiKey = stripeKey;
                    System.out.println("✅ Stripe für Paywall initialisiert mit Schlüssel-Index: " + index);
                }
            } else {
                System.out.println("ℹ️ Keine Stripe-Schlüssel konfiguriert → nur manueller Zahlungsweg aktiv.");
            }
            System.out.println("✅ Paywall Service online | domain=" + domain + " | owner=" + owner + " | vault=" + vaultDir);
        };
    }

    // All other inner classes remain unchanged. They will use the globally set Stripe.apiKey.

    static record Product(String sku, String name, int priceCentsEUR, String kind, String fileName) {
        void enforce() {
            if ("SUB".equalsIgnoreCase(kind) && priceCentsEUR < 9_900_000) {
                throw new IllegalArgumentException("Mindestpreis für SUB ist 99.000 EUR/Monat.");
            }
        }
    }

    static record Order(String id, String sku, String buyerEmail, String status, long created, String fulfillToken) { }
    static record Audit(String when, String who, String action, String detail, String ip) { }

    @Service
    static class CatalogService {
        private final Map<String, Product> catalog = new ConcurrentHashMap<>();
        private final Path vaultDir;

        CatalogService(@Value("${app.vaultDir:./vault}") String vaultDir) {
            this.vaultDir = Path.of(vaultDir);
            put(new Product("SUB-ENTERPRISE", "Enterprise Monatsabo", 9_900_000, "SUB", "monthly-enterprise.zip"));
            put(new Product("ONBOARD", "Onboarding Paket (einmalig)", 19_900_00, "ONE", "onboard.zip"));
        }

        public void put(Product p) { p.enforce(); catalog.put(p.sku().toUpperCase(), p); }
        public Product get(String sku) {
            Product p = catalog.get(sku.toUpperCase());
            if (p == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "SKU unbekannt");
            return p;
        }
        public Collection<Product> list() { return catalog.values(); }
        public Path resolveFile(Product p) { return vaultDir.resolve(p.fileName()); }
    }

    @Service
    static class OrderService {
        private final SecretKey jwtKey;
        private final String domain;
        private final Map<String, Order> orders = new ConcurrentHashMap<>();

        OrderService(@Value("${app.jwtSecret}") String jwtSecret, @Value("${app.domain}") String domain) {
            if (jwtSecret == null || jwtSecret.length() < 64) {
                throw new IllegalStateException("app.jwtSecret zu kurz (>=64 Zeichen hex empfohlen).");
            }
            this.jwtKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
            this.domain = domain;
        }

        public Order create(String sku, String buyerEmail) {
            String id = UUID.randomUUID().toString();
            Order o = new Order(id, sku, buyerEmail, "PENDING", Instant.now().getEpochSecond(), null);
            orders.put(id, o);
            return o;
        }

        public void markPaid(String orderId) {
            Order o = orders.get(orderId);
            if (o == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Order nicht gefunden");
            orders.put(orderId, new Order(o.id(), o.sku(), o.buyerEmail(), "PAID", o.created(), null));
        }

        public String issueDownloadToken(String sku, String buyerEmail, int ttlSeconds) {
            return Jwts.builder()
                .claim("sku", sku.toUpperCase()).claim("mail", buyerEmail == null ? "" : buyerEmail)
                .claim("jti", UUID.randomUUID().toString()).expiration(Date.from(Instant.now().plusSeconds(ttlSeconds)))
                .signWith(jwtKey).compact();
        }

        public Map<String, Object> validateToken(String token) {
            var body = Jwts.parserBuilder().setSigningKey(jwtKey).build().parseClaimsJws(token).getBody();
            Map<String, Object> m = new HashMap<>();
            m.put("sku", String.valueOf(body.get("sku")));
            m.put("mail", String.valueOf(body.get("mail")));
            return m;
        }

        public String linkFromToken(String token) { return domain + "/download/" + token; }
        public Collection<Order> list() { return orders.values(); }
    }

    @Service
    static class AuditService {
        private final List<Audit> entries = Collections.synchronizedList(new ArrayList<>());
        public void add(String who, String action, String detail, String ip) { entries.add(new Audit(LocalDateTime.now(ZoneOffset.UTC).toString(), who, action, detail, ip)); }
        public List<Audit> tail(int n) { int from = Math.max(entries.size() - n, 0); return entries.subList(from, entries.size()); }
    }

    @Service
    static class AdminConfig {
        private volatile String adminToken;
        private volatile String openaiApiKey;
        private volatile String revolutPayLink;
        private volatile String iban, bic, bank;

        AdminConfig(@Value("${app.adminToken:CHANGE_ME_ADMIN}") String adminToken, @Value("${pay.revolut.link:https://revolut.me/sanel92}") String rev, @Value("${pay.iban:LT723250010548966150}") String iban, @Value("${pay.bic:REVOLT21}") String bic, @Value("${pay.bank:Revolut Bank UAB, Konstitucijos ave. 21B, 08130 Vilnius, LT}") String bank) {
            this.adminToken = adminToken;
            this.revolutPayLink = rev; this.iban = iban; this.bic = bic; this.bank = bank;
        }

        boolean isAdmin(String token) { return token != null && token.equals(adminToken); }
        Map<String, Object> view() { return Map.of("revolutLink", revolutPayLink, "iban", iban, "bic", bic, "bank", bank, "openaiApiKey_set", openaiApiKey != null && !openaiApiKey.isBlank()); }
        void setOpenAIKey(String key) { this.openaiApiKey = key; }
        String openAIKey() { return openaiApiKey; }
        void setAdminToken(String t) { this.adminToken = t; }
        void setPaymentRefs(String rev, String iban, String bic, String bank) { this.revolutPayLink = rev; this.iban = iban; this.bic = bic; this.bank = bank; }
    }

    static class RateLimiter {
        private final int maxPerMin;
        private final Map<String, Deque<Long>> map = new ConcurrentHashMap<>();
        RateLimiter(int maxPerMin) { this.maxPerMin = maxPerMin; }
        synchronized boolean allow(String key) {
            long now = System.currentTimeMillis(), win = now - 60_000L;
            Deque<Long> q = map.computeIfAbsent(key, k -> new ArrayDeque<>());
            while (!q.isEmpty() && q.peekFirst() < win) q.pollFirst();
            if (q.size() >= maxPerMin) return false;
            q.addLast(now);
            return true;
        }
    }

    static void require(boolean cond, String msg, HttpStatus status) {
        if (!cond) throw new ResponseStatusException(status, msg);
    }

    @RestController
    @RequestMapping("/paywall")
    static class PublicController {
        private final CatalogService catalog; private final OrderService orders; private final AuditService audit; private final AdminConfig admin;
        private final String domain; private final RateLimiter rl = new RateLimiter(60);

        PublicController(CatalogService c, OrderService o, AuditService a, AdminConfig ad, @Value("${app.domain}") String d) {
            this.catalog = c; this.orders = o; this.audit = a; this.admin = ad; this.domain = d;
        }

        @GetMapping("/quote")
        public Map<String,Object> quote(@RequestHeader(value="X-Forwarded-For", required=false) String xfip) {
            String ip = xfip != null ? xfip : "public";
            if (!rl.allow("quote:"+ip)) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Zu viele Anfragen");
            List<Map<String,Object>> list = new ArrayList<>();
            for (Product p : catalog.list()) {
                list.add(Map.of("sku", p.sku(), "name", p.name(), "kind", p.kind(), "price_eur", p.priceCentsEUR()/100.0, "pay", domain + "/paywall/payinfo?sku=" + p.sku()));
            }
            return Map.of("owner", "Sanel Crnkic", "products", list);
        }

        @GetMapping("/payinfo")
        public Map<String,String> payinfo(@RequestParam String sku, @RequestParam(required=false) String email, @RequestHeader(value="X-Forwarded-For", required=false) String xfip) throws Exception {
            String ip = xfip != null ? xfip : "public";
            if (!rl.allow("payinfo:"+ip)) throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Zu viele Anfragen");

            Product p = catalog.get(sku);
            Order o = orders.create(p.sku(), email == null ? "" : email);
            audit.add(email == null ? "-" : email, "ORDER_CREATED", p.sku(), ip);

            // The Stripe.apiKey is now globally set. We just check if it's available.
            if (Stripe.apiKey == null || Stripe.apiKey.isBlank()) {
                String memo = "ORDER#" + o.id();
                String manual = "Überweise bitte " + (p.priceCentsEUR()/100.0) + " EUR\nIBAN: " + admin.iban + "\nBIC: " + admin.bic + "\nBank: " + admin.bank + "\nOder Revolut: " + admin.revolutPayLink + "\nVerwendungszweck: " + memo + "\nNach Zahlung: Screenshot/Beleg + ORDER-ID an Support schicken.";
                return Map.of("mode","manual", "order_id", o.id(), "instructions", manual);
            }

            SessionCreateParams.LineItem.PriceData.ProductData prod = SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(p.name()).build();
            SessionCreateParams.LineItem.PriceData price = SessionCreateParams.LineItem.PriceData.builder().setCurrency("eur").setUnitAmount((long) p.priceCentsEUR()).setProductData(prod).build();
            SessionCreateParams.Mode mode = "SUB".equalsIgnoreCase(p.kind()) ? SessionCreateParams.Mode.SUBSCRIPTION : SessionCreateParams.Mode.PAYMENT;
            SessionCreateParams params = SessionCreateParams.builder().setMode(mode).addLineItem(SessionCreateParams.LineItem.builder().setQuantity(1L).setPriceData(price).build()).putMetadata("order_id", o.id()).setSuccessUrl(domain + "/paywall/success?order_id=" + o.id() + "&session_id={CHECKOUT_SESSION_ID}").setCancelUrl(domain + "/paywall/cancel").build();
            Session session = Session.create(params);
            return Map.of("mode","stripe","checkout_url", session.getUrl(), "order_id", o.id());
        }

        @GetMapping("/success")
        public ResponseEntity<String> success(@RequestParam String order_id, @RequestParam("session_id") String sid, @RequestHeader(value="X-Forwarded-For", required=false) String xfip) {
            String ip = xfip != null ? xfip : "public";
            orders.markPaid(order_id);
            Order found = orders.list().stream().filter(o -> o.id().equals(order_id)).findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order nicht gefunden"));
            String token = orders.issueDownloadToken(found.sku(), found.buyerEmail(), 240);
            h.setContentLength(zipped.length);
            h.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            return new ResponseEntity<>(zipped, h, HttpStatus.OK);
        }

        private byte[] buildWatermarkedZip(Path original, String buyer, String sku) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ZipOutputStream zos = new ZipOutputStream(bos)) {
                ZipEntry orig = new ZipEntry(original.getFileName().toString());
                zos.putNextEntry(orig);
                Files.copy(original, zos);
                zos.closeEntry();
                ZipEntry receipt = new ZipEntry("RECEIPT.txt");
                zos.putNextEntry(receipt);
                String mark = "Owner: Sanel Crnkic\nSKU: " + sku + "\nBuyer: " + (buyer == null ? "-" : buyer) + "\nIssued: " + Instant.now() + "Z\nLegal: Nur für den Käufer bestimmt. Weitergabe untersagt.\n";
                zos.write(mark.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
            return bos.toByteArray();
        }
    }

    @RestController
    @RequestMapping("/admin/paywall")
    static class AdminController {
        private final CatalogService catalog; private final OrderService orders; private final AuditService audit; private final AdminConfig admin;
        AdminController(CatalogService c, OrderService o, AuditService a, AdminConfig ad) { this.catalog = c; this.orders = o; this.audit = a; this.admin = ad; }
        private void auth(String token) { require(admin.isAdmin(token), "Admin-Token fehlt/ungültig", HttpStatus.UNAUTHORIZED); }

        @GetMapping("/config") public Map<String, Object> config(@RequestHeader("X-Admin-Token") String t) { auth(t); return admin.view(); }
        @PostMapping("/openai") public Map<String,String> setOpenAI(@RequestHeader("X-Admin-Token") String t, @RequestParam String apiKey) { auth(t); admin.setOpenAIKey(apiKey); audit.add("admin", "SET_OPENAI_KEY", "len=" + apiKey.length(), "local"); return Map.of("ok","saved","openaiApiKey_set","true"); }
        @PostMapping("/admin-token") public Map<String,String> rotateAdminToken(@RequestHeader("X-Admin-Token") String t, @RequestParam String newToken) { auth(t); admin.setAdminToken(newToken); return Map.of("ok","rotated"); }
        @PostMapping("/payrefs") public Map<String,String> setPayRefs(@RequestHeader("X-Admin-Token") String t, @RequestParam String revolut, @RequestParam String iban, @RequestParam String bic, @RequestParam String bank) { auth(t); admin.setPaymentRefs(revolut, iban, bic, bank); return Map.of("ok","saved"); }
        @PostMapping("/product") public Map<String,Object> upsertProduct(@RequestHeader("X-Admin-Token") String t, @RequestParam String sku, @RequestParam String name, @RequestParam int priceCentsEUR, @RequestParam String kind, @RequestParam String fileName) { auth(t); Product p = new Product(sku.toUpperCase(), name, priceCentsEUR, kind.toUpperCase(), fileName); p.enforce(); catalog.put(p); audit.add("admin","UPSERT_PRODUCT", sku, "local"); return Map.of("ok","saved","sku",p.sku(),"price_eur",p.priceCentsEUR()/100.0,"kind",p.kind()); }
        @PostMapping("/manual-issue") public Map<String,String> manualIssue(@RequestHeader("X-Admin-Token") String t, @RequestParam String sku, @RequestParam(required=false) String buyerEmail) { auth(t); orders.create(sku, buyerEmail); String token = orders.issueDownloadToken(sku, buyerEmail, 600); String link = orders.linkFromToken(token); audit.add("admin","MANUAL_ISSUE", sku + " to " + buyerEmail, "local"); return Map.of("download", link); }
        @GetMapping("/orders") public Collection<Order> orders(@RequestHeader("X-Admin-Token") String t) { auth(t); return orders.list(); }
        @GetMapping("/audit") public List<Audit> audit(@RequestHeader("X-Admin-Token") String t, @RequestParam(defaultValue = "25") int tail) { auth(t); return new ArrayList<>(audit.tail(Math.max(1, Math.min(200, tail)))); }
    }
}
public class Server {
  public static void main(String[] args) throws StripeException {
    Stripe.apiKey = "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";


    ProductCreateParams productParams =
      ProductCreateParams.builder()
        .setName("Starter Subscription")
        .setDescription("$99.0000/Month subscription")
        .build();
    Product product = Product.create(productParams);
    System.out.println("Success! Here is your starter subscription product id: " + product.getId());

    PriceCreateParams params =
      PriceCreateParams
        .builder()
        .setProduct(product.getId())
        .setCurrency("usd")
        .setUnitAmount(1200L)
        .setRecurring(
          PriceCreateParams.Recurring
            .builder()
            .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
            .build())
        .build();
    Price price = Price.create(params);
    System.out.println("Success! Here is your starter subscription price id: " + price.getId());
  }
}
