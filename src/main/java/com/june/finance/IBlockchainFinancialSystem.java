package com.june.finance;

public interface IBlockchainFinancialSystem {
    void allocateFundsForDevelopment(int requiredFunds) throws FundingAllocationException;
}
