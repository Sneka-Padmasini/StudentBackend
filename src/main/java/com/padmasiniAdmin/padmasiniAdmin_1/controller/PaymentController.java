package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.razorpay.Payment;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.padmasiniAdmin.padmasiniAdmin_1.dto.OrderRequest; 
import com.padmasiniAdmin.padmasiniAdmin_1.dto.PaymentVerificationRequest; 
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;
    
    // --- 1. Order Creation Endpoint ---
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            long amountInPaise = orderRequest.getAmount() * 100;
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            
            JSONObject orderRequestJson = new JSONObject();
            orderRequestJson.put("amount", amountInPaise);
            orderRequestJson.put("currency", "INR");
            orderRequestJson.put("receipt", "txn_" + System.currentTimeMillis());
            orderRequestJson.put("payment_capture", 1); 
            
            JSONObject notes = new JSONObject();
            notes.put("plan_type", orderRequest.getPlan());
            orderRequestJson.put("notes", notes);

            // 🔥 FIX 1: Changed .Orders to .orders (lowercase)
            Order order = razorpay.orders.create(orderRequestJson); 
            
            return ResponseEntity.ok(order.toString()); 

        } catch (Exception e) {
            System.err.println("Error creating Razorpay order: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new JSONObject().put("error", "Failed to create Razorpay order."));
        }
    }

    // --- 2. Payment Verification Endpoint ---
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(@RequestBody PaymentVerificationRequest verificationRequest) {
        String generatedSignature = verificationRequest.getRazorpaySignature();
        String orderId = verificationRequest.getRazorpayOrderId();
        String paymentId = verificationRequest.getRazorpayPaymentId();
        
        String secret = razorpayKeySecret;
        
        
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", generatedSignature);

            boolean isVerified = Utils.verifyPaymentSignature(options, secret);

            if (isVerified) {
                // ✅ FETCH PAYMENT DETAILS FROM RAZORPAY TO GET UPI ID
                RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
                Payment payment = razorpay.payments.fetch(paymentId);
                
                String payerInfo = "";
                
                // Try to get VPA (UPI ID)
                if (payment.has("vpa")) {
                    payerInfo = payment.get("vpa").toString();
                } 
                // If not UPI, try email or contact
                else if (payment.has("email")) {
                    payerInfo = payment.get("email").toString();
                }

                System.out.println("Payment verified. Payer: " + payerInfo);
                
                // ✅ RETURN THE PAYER INFO TO FRONTEND
                JSONObject response = new JSONObject();
                response.put("message", "Payment verified");
                response.put("payerId", payerInfo); // Send back UPI ID
                
                return ResponseEntity.ok(response.toString());
            } else {
                return ResponseEntity.badRequest().body("Signature mismatch.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Verification error.");
        }
    }
}
