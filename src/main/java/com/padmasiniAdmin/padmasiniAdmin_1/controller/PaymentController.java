package com.padmasiniAdmin.padmasiniAdmin_1.controller;

import com.razorpay.Payment;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import com.padmasiniAdmin.padmasiniAdmin_1.dto.OrderRequest; 
import com.padmasiniAdmin.padmasiniAdmin_1.dto.PaymentVerificationRequest; 
// ✅ Import EmailService
import com.padmasiniAdmin.padmasiniAdmin_1.service.EmailService; 

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired; 
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
    
    // ✅ Inject EmailService to send mails
    @Autowired
    private EmailService emailService;
    
    // --- 1. Order Creation Endpoint ---
    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest orderRequest) {
        try {
            // ✅ LOGIC UPDATE: Calculate 18% GST (9% CGST + 9% SGST)
            // We take the Base Price (e.g., 33000) and add 18% here.
            long baseAmount = orderRequest.getAmount();
            long totalAmountWithGst = (long) (baseAmount * 1.18); 
            
            // Convert to Paise for Razorpay (e.g. 38940 * 100)
            long amountInPaise = totalAmountWithGst * 100; 
            
            RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            
            JSONObject orderRequestJson = new JSONObject();
            orderRequestJson.put("amount", amountInPaise);
            orderRequestJson.put("currency", "INR");
            orderRequestJson.put("receipt", "txn_" + System.currentTimeMillis());
            orderRequestJson.put("payment_capture", 1); 
            
            JSONObject notes = new JSONObject();
            notes.put("plan_type", orderRequest.getPlan());
            orderRequestJson.put("notes", notes);

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
                // ✅ FETCH PAYMENT DETAILS FROM RAZORPAY
                RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
                Payment payment = razorpay.payments.fetch(paymentId);
                
                String payerInfo = "";
                String payerEmail = verificationRequest.getUserId(); // Default from frontend
                String payerContact = "N/A";
                String amountPaid = "0";

                // Extract Details safely
                if (payment.has("email")) {
                    payerEmail = payment.get("email").toString();
                }
                if (payment.has("contact")) {
                    payerContact = payment.get("contact").toString();
                }
                if (payment.has("amount")) {
                    // Razorpay amount is in paise, convert to Rupees
                    long amt = Long.parseLong(payment.get("amount").toString());
                    amountPaid = String.valueOf(amt / 100);
                }

                // Try to get VPA (UPI ID) or fallback to email
                if (payment.has("vpa")) {
                    payerInfo = payment.get("vpa").toString();
                } else if (payment.has("email")) {
                    payerInfo = payment.get("email").toString();
                }

                System.out.println("Payment verified. Payer: " + payerInfo);
                
                // ✅ SEND IMPROVED EMAIL (Pass User Name if available, or use Email as name)
                try {
                    // Note: 'verificationRequest' doesn't have name, so we use email as fallback for name
                    // If you want name, you'd need to add 'userName' to PaymentVerificationRequest DTO
                    String userName = payerEmail.split("@")[0]; 

                    emailService.sendSubscriptionSuccessEmail(
                        "learnforward@padmasini.com", // Admin Email
                        payerEmail,                   // User Email
                        userName,                     // User Name (Extracted from email)
                        verificationRequest.getPlan(),
                        paymentId,
                        payerContact,
                        amountPaid
                    );
                } catch (Exception e) {
                    System.err.println("⚠️ Warning: Email notification failed, but payment was successful.");
                    e.printStackTrace();
                }
                
                // ✅ RETURN RESPONSE TO FRONTEND
                JSONObject response = new JSONObject();
                response.put("message", "Payment verified");
                response.put("payerId", payerInfo); 
                
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
