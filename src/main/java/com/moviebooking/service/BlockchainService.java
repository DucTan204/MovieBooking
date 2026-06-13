package com.moviebooking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class BlockchainService {

    private static final Logger log = LoggerFactory.getLogger(BlockchainService.class);

    @Value("${blockchain.node.enabled:false}")
    private boolean enabled;

    @Value("${blockchain.node.url:http://localhost:8545}")
    private String nodeUrl;

    @Value("${blockchain.contract.address:0x0}")
    private String contractAddress;

    /**
     * Ghi hash vé lên blockchain.
     * Khi blockchain.node.enabled=false → chạy mock mode (trả về hash giả).
     */
    public String registerTicketHash(String qrCode, Long bookingId) {
        if (!enabled) {
            // Mock mode cho development
            String mockTxHash = "0xMOCK_" + Long.toHexString(bookingId) + "_" + qrCode.substring(0, 8);
            log.info("[BLOCKCHAIN-MOCK] Registered ticket hash: {} → txHash: {}", qrCode, mockTxHash);
            return mockTxHash;
        }

        // TODO: Tích hợp web3j khi deploy thật
        // Web3j web3 = Web3j.build(new HttpService(nodeUrl));
        // ...
        log.warn("[BLOCKCHAIN] Node enabled but web3j not integrated yet.");
        return null;
    }

    /**
     * Kiểm tra hash vé trên blockchain.
     */
    public boolean verifyTicketHash(String qrCode) {
        if (!enabled) {
            log.info("[BLOCKCHAIN-MOCK] Verify ticket: {}", qrCode);
            return true; // mock: luôn hợp lệ
        }
        // TODO: verify on-chain
        return true;
    }
}