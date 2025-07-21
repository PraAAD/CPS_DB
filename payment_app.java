// MainActivity.java
package com.OnePay.paymentapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private TextView welcomeText, balanceText;
    private Button generateQRBtn, sendMoneyBtn, receiveMoneyBtn, 
                   transactionHistoryBtn, chatbotBtn;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        dbHelper = new DatabaseHelper(this);
        initializeViews();
        setupClickListeners();
        loadCurrentUser();
    }
    
    private void initializeViews() {
        welcomeText = findViewById(R.id.welcomeText);
        balanceText = findViewById(R.id.balanceText);
        generateQRBtn = findViewById(R.id.generateQRBtn);
        sendMoneyBtn = findViewById(R.id.sendMoneyBtn);
        receiveMoneyBtn = findViewById(R.id.receiveMoneyBtn);
        transactionHistoryBtn = findViewById(R.id.transactionHistoryBtn);
        chatbotBtn = findViewById(R.id.chatbotBtn);
    }
    
    private void setupClickListeners() {
        generateQRBtn.setOnClickListener(v -> 
            startActivity(new Intent(this, QRCodeActivity.class)));
        
        sendMoneyBtn.setOnClickListener(v -> 
            startActivity(new Intent(this, SendMoneyActivity.class)));
        
        receiveMoneyBtn.setOnClickListener(v -> 
            startActivity(new Intent(this, ReceiveMoneyActivity.class)));
        
        transactionHistoryBtn.setOnClickListener(v -> 
            startActivity(new Intent(this, TransactionHistoryActivity.class)));
        
        chatbotBtn.setOnClickListener(v -> 
            startActivity(new Intent(this, ChatbotActivity.class)));
    }
    
    private void loadCurrentUser() {
        // Load current user data and display balance
        Customer currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            welcomeText.setText("Welcome, " + currentUser.getName());
            balanceText.setText("Balance: $" + currentUser.getBalance());
        }
    }
}

// DatabaseHelper.java
package com.OnePay.paymentapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "OnePay.db";
    private static final int DATABASE_VERSION = 1;
    
    // Customer table
    private static final String TABLE_CUSTOMERS = "customers";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_MOBILE = "mobile";
    private static final String COLUMN_ACCOUNT_NUMBER = "account_number";
    private static final String COLUMN_UNIQUE_KEY = "unique_key";
    private static final String COLUMN_EMAIL_ID = "email_id";
    private static final String COLUMN_BALANCE = "balance";
    private static final String COLUMN_UNIQUE_10_DIGIT = "unique_10_digit";
    
    // Transaction table
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COLUMN_TRANS_ID = "transaction_id";
    private static final String COLUMN_FROM_CUSTOMER = "from_customer";
    private static final String COLUMN_TO_CUSTOMER = "to_customer";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_STATUS = "status";
    
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create customers table
        String CREATE_CUSTOMERS_TABLE = "CREATE TABLE " + TABLE_CUSTOMERS + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_MOBILE + " TEXT UNIQUE,"
                + COLUMN_ACCOUNT_NUMBER + " TEXT UNIQUE,"
                + COLUMN_UNIQUE_KEY + " TEXT UNIQUE,"
                + COLUMN_EMAIL_ID + " TEXT UNIQUE,"
                + COLUMN_BALANCE + " REAL,"
                + COLUMN_UNIQUE_10_DIGIT + " TEXT UNIQUE" + ")";
        
        // Create transactions table
        String CREATE_TRANSACTIONS_TABLE = "CREATE TABLE " + TABLE_TRANSACTIONS + "("
                + COLUMN_TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_FROM_CUSTOMER + " TEXT,"
                + COLUMN_TO_CUSTOMER + " TEXT,"
                + COLUMN_AMOUNT + " REAL,"
                + COLUMN_TIMESTAMP + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_STATUS + " TEXT" + ")";
        
        db.execSQL(CREATE_CUSTOMERS_TABLE);
        db.execSQL(CREATE_TRANSACTIONS_TABLE);
        
        // Insert sample data
        insertSampleData(db);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CUSTOMERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        onCreate(db);
    }
    
    private void insertSampleData(SQLiteDatabase db) {
        String[] names = {"John Doe", "Jane Smith", "Mike Johnson", "Sarah Wilson", 
                         "David Brown", "Emily Davis", "Chris Miller", "Lisa Garcia",
                         "Robert Martinez", "Amanda Taylor"};
        
        String[] mobiles = {"1234567890", "2345678901", "3456789012", "4567890123",
                           "5678901234", "6789012345", "7890123456", "8901234567",
                           "9012345678", "0123456789"};
        
        for (int i = 0; i < 10; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_NAME, names[i]);
            values.put(COLUMN_MOBILE, mobiles[i]);
            values.put(COLUMN_ACCOUNT_NUMBER, "ACC" + String.format("%06d", i + 1));
            values.put(COLUMN_UNIQUE_KEY, generateUniqueKey());
            values.put(COLUMN_EMAIL_ID, generateUniqueKey() + "@OnePay.com");
            values.put(COLUMN_BALANCE, 1000.0 + (i * 500)); // Starting balance
            values.put(COLUMN_UNIQUE_10_DIGIT, generateUnique10Digit());
            
            db.insert(TABLE_CUSTOMERS, null, values);
        }
    }
    
    private String generateUniqueKey() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder result = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 7; i++) {
            result.append(chars.charAt(random.nextInt(chars.length())));
        }
        return result.toString();
    }
    
    private String generateUnique10Digit() {
        Random random = new Random();
        long number = 1000000000L + (long)(random.nextDouble() * 9000000000L);
        return String.valueOf(number);
    }
    
    public Customer getCustomerByMobile(String mobile) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, 
                COLUMN_MOBILE + "=?", new String[]{mobile}, 
                null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            Customer customer = new Customer();
            customer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            customer.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            customer.setMobile(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOBILE)));
            customer.setAccountNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT_NUMBER)));
            customer.setUniqueKey(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_KEY)));
            customer.setEmailId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL_ID)));
            customer.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BALANCE)));
            customer.setUnique10Digit(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_10_DIGIT)));
            
            cursor.close();
            return customer;
        }
        
        if (cursor != null) cursor.close();
        return null;
    }
    
    public Customer getCustomerByEmailId(String emailId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, 
                COLUMN_EMAIL_ID + "=?", new String[]{emailId}, 
                null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            Customer customer = new Customer();
            customer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            customer.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
            customer.setMobile(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOBILE)));
            customer.setAccountNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT_NUMBER)));
            customer.setUniqueKey(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_KEY)));
            customer.setEmailId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL_ID)));
            customer.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BALANCE)));
            customer.setUnique10Digit(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_10_DIGIT)));
            
            cursor.close();
            return customer;
        }
        
        if (cursor != null) cursor.close();
        return null;
    }
    
    public boolean updateBalance(String mobile, double newBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BALANCE, newBalance);
        
        int result = db.update(TABLE_CUSTOMERS, values, 
                COLUMN_MOBILE + "=?", new String[]{mobile});
        return result > 0;
    }
    
    public long addTransaction(String fromCustomer, String toCustomer, 
                             double amount, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FROM_CUSTOMER, fromCustomer);
        values.put(COLUMN_TO_CUSTOMER, toCustomer);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_STATUS, status);
        
        return db.insert(TABLE_TRANSACTIONS, null, values);
    }
    
    public List<Transaction> getTransactionHistory(String customerMobile) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_TRANSACTIONS, null, 
                COLUMN_FROM_CUSTOMER + "=? OR " + COLUMN_TO_CUSTOMER + "=?", 
                new String[]{customerMobile, customerMobile}, 
                null, null, COLUMN_TIMESTAMP + " DESC");
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Transaction transaction = new Transaction();
                transaction.setTransactionId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TRANS_ID)));
                transaction.setFromCustomer(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FROM_CUSTOMER)));
                transaction.setToCustomer(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TO_CUSTOMER)));
                transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT)));
                transaction.setTimestamp(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                transaction.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                
                transactions.add(transaction);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        return transactions;
    }
    
    public List<Customer> getAllCustomers() {
        List<Customer> customers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        Cursor cursor = db.query(TABLE_CUSTOMERS, null, null, null, null, null, null);
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                Customer customer = new Customer();
                customer.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                customer.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                customer.setMobile(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOBILE)));
                customer.setAccountNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACCOUNT_NUMBER)));
                customer.setUniqueKey(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_KEY)));
                customer.setEmailId(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL_ID)));
                customer.setBalance(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_BALANCE)));
                customer.setUnique10Digit(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_10_DIGIT)));
                
                customers.add(customer);
            } while (cursor.moveToNext());
            
            cursor.close();
        }
        
        return customers;
    }
}

// Customer.java
package com.OnePay.paymentapp;

public class Customer {
    private int id;
    private String name;
    private String mobile;
    private String accountNumber;
    private String uniqueKey;
    private String emailId;
    private double balance;
    private String unique10Digit;
    
    // Constructors
    public Customer() {}
    
    public Customer(String name, String mobile, String accountNumber, 
                   String uniqueKey, String emailId, double balance, String unique10Digit) {
        this.name = name;
        this.mobile = mobile;
        this.accountNumber = accountNumber;
        this.uniqueKey = uniqueKey;
        this.emailId = emailId;
        this.balance = balance;
        this.unique10Digit = unique10Digit;
    }
    
    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
    
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public String getUniqueKey() { return uniqueKey; }
    public void setUniqueKey(String uniqueKey) { this.uniqueKey = uniqueKey; }
    
    public String getEmailId() { return emailId; }
    public void setEmailId(String emailId) { this.emailId = emailId; }
    
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    
    public String getUnique10Digit() { return unique10Digit; }
    public void setUnique10Digit(String unique10Digit) { this.unique10Digit = unique10Digit; }
    
    // Generate QR Code data
    public String generateQRData() {
        return mobile + accountNumber + unique10Digit;
    }
}

// Transaction.java
package com.OnePay.paymentapp;

public class Transaction {
    private int transactionId;
    private String fromCustomer;
    private String toCustomer;
    private double amount;
    private String timestamp;
    private String status;
    
    // Constructors
    public Transaction() {}
    
    public Transaction(String fromCustomer, String toCustomer, double amount, String status) {
        this.fromCustomer = fromCustomer;
        this.toCustomer = toCustomer;
        this.amount = amount;
        this.status = status;
    }
    
    // Getters and Setters
    public int getTransactionId() { return transactionId; }
    public void setTransactionId(int transactionId) { this.transactionId = transactionId; }
    
    public String getFromCustomer() { return fromCustomer; }
    public void setFromCustomer(String fromCustomer) { this.fromCustomer = fromCustomer; }
    
    public String getToCustomer() { return toCustomer; }
    public void setToCustomer(String toCustomer) { this.toCustomer = toCustomer; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

// QRCodeActivity.java
package com.OnePay.paymentapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

public class QRCodeActivity extends AppCompatActivity {
    private ImageView qrCodeImageView;
    private TextView qrDataTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);
        
        qrCodeImageView = findViewById(R.id.qrCodeImageView);
        qrDataTextView = findViewById(R.id.qrDataTextView);
        
        generateQRCode();
    }
    
    private void generateQRCode() {
        Customer currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            String qrData = currentUser.generateQRData();
            qrDataTextView.setText("QR Data: " + qrData);
            
            try {
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, BarcodeFormat.QR_CODE, 400, 400);
                qrCodeImageView.setImageBitmap(bitmap);
            } catch (WriterException e) {
                e.printStackTrace();
            }
        }
    }
}

// SendMoneyActivity.java
package com.OnePay.paymentapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SendMoneyActivity extends AppCompatActivity {
    private EditText recipientEditText, amountEditText;
    private Button sendMoneyBtn;
    private DatabaseHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);
        
        dbHelper = new DatabaseHelper(this);
        
        recipientEditText = findViewById(R.id.recipientEditText);
        amountEditText = findViewById(R.id.amountEditText);
        sendMoneyBtn = findViewById(R.id.sendMoneyBtn);
        
        sendMoneyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMoney();
            }
        });
    }
    
    private void sendMoney() {
        String recipient = recipientEditText.getText().toString().trim();
        String amountStr = amountEditText.getText().toString().trim();
        
        if (TextUtils.isEmpty(recipient) || TextUtils.isEmpty(amountStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }
        
        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Customer currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (currentUser.getBalance() < amount) {
            Toast.makeText(this, "Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Customer recipientCustomer = null;
        
        // Check if recipient is mobile number or email ID
        if (recipient.contains("@OnePay.com")) {
            recipientCustomer = dbHelper.getCustomerByEmailId(recipient);
        } else {
            recipientCustomer = dbHelper.getCustomerByMobile(recipient);
        }
        
        if (recipientCustomer == null) {
            Toast.makeText(this, "Recipient not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Process transaction
        boolean senderUpdated = dbHelper.updateBalance(currentUser.getMobile(), 
                currentUser.getBalance() - amount);
        boolean recipientUpdated = dbHelper.updateBalance(recipientCustomer.getMobile(), 
                recipientCustomer.getBalance() + amount);
        
        if (senderUpdated && recipientUpdated) {
            // Record transaction
            dbHelper.addTransaction(currentUser.getMobile(), recipientCustomer.getMobile(), 
                    amount, "SUCCESS");
            
            // Update session
            currentUser.setBalance(currentUser.getBalance() - amount);
            SessionManager.setCurrentUser(currentUser);
            
            Toast.makeText(this, "Money sent successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Transaction failed", Toast.LENGTH_SHORT).show();
        }
    }
}

// ReceiveMoneyActivity.java
package com.OnePay.paymentapp;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ReceiveMoneyActivity extends AppCompatActivity {
    private TextView receiveInfoTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_money);
        
        receiveInfoTextView = findViewById(R.id.receiveInfoTextView);
        
        Customer currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            String receiveInfo = "Share your details to receive money:\n\n" +
                    "Mobile Number: " + currentUser.getMobile() + "\n" +
                    "Email ID: " + currentUser.getEmailId() + "\n\n" +
                    "Or share your QR code for easy payment";
            
            receiveInfoTextView.setText(receiveInfo);
        }
    }
}

// TransactionHistoryActivity.java
package com.OnePay.paymentapp;

import android.os.Bundle;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {
    private ListView transactionListView;
    private DatabaseHelper dbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);
        
        dbHelper = new DatabaseHelper(this);
        transactionListView = findViewById(R.id.transactionListView);
        
        loadTransactionHistory();
    }
    
    private void loadTransactionHistory() {
        Customer currentUser = SessionManager.getCurrentUser();
        if (currentUser != null) {
            List<Transaction> transactions = dbHelper.getTransactionHistory(currentUser.getMobile());
            TransactionAdapter adapter = new TransactionAdapter(this, transactions, currentUser.getMobile());
            transactionListView.setAdapter(adapter);
        }
    }
}

// TransactionAdapter.java
package com.OnePay.paymentapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.List;

public class TransactionAdapter extends BaseAdapter {
    private Context context;
    private List<Transaction> transactions;
    private String currentUserMobile;
    
    public TransactionAdapter(Context context, List<Transaction> transactions, String currentUserMobile) {
        this.context = context;
        this.transactions = transactions;
        this.currentUserMobile = currentUserMobile;
    }
    
    @Override
    public int getCount() {
        return transactions.size();
    }
    
    @Override
    public Object getItem(int position) {
        return transactions.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.transaction_item, parent, false);
        }
        
        Transaction transaction = transactions.get(position);
        
        TextView typeTextView = convertView.findViewById(R.id.typeTextView);
        TextView amountTextView = convertView.findViewById(R.id.amountTextView);
        TextView detailsTextView = convertView.findViewById(R.id.detailsTextView);
        TextView timestampTextView = convertView.findViewById(R.id.timestampTextView);
        
        boolean isSent = transaction.getFromCustomer().equals(currentUserMobile);
        
        typeTextView.setText(isSent ? "SENT" : "RECEIVED");
        amountTextView.setText("$" + transaction.getAmount());
        detailsTextView.setText(isSent ? "To: " + transaction.getToCustomer() : "From: " + transaction.getFromCustomer());
        timestampTextView.setText(transaction.getTimestamp());
        
        return convertView;
    }
}

// ChatbotActivity.java
package com.OnePay.paymentapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ChatbotActivity extends AppCompatActivity {
    private ListView chatListView;
    private EditText messageEditText;
    private Button sendButton;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);
        
        chatListView = findViewById(R.id.chatListView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);
        chatListView.setAdapter(chatAdapter);
        
        // Welcome message
        addBotMessage("Hi! I'm your XYZ Bank assistant. I can help you with:\n" +
                "1. How to send money\n" +
                "2. How to receive money\n" +
                "3. QR code payments\n" +
                "4. Transaction history\n" +
                "5. Account balance\n\n" +
                "What would you like to know?");
        
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }
    
    private void sendMessage() {
        String message = messageEditText.getText().toString().trim();
        if (!message.isEmpty()) {
            addUserMessage(message);
            messageEditText.setText("");
            processBotResponse(message);
        }
    }
    
    private void addUserMessage(String message) {
        chatMessages.add(new ChatMessage(message, true));
        chatAdapter.notifyDataSetChanged();
        chatListView.smoothScrollToPosition(chatMessages.size() - 1);
    }
    
    private void addBotMessage(String message) {
        chatMessages.add(new ChatMessage(message, false));
        chatAdapter.notifyDataSetChanged();
        chatListView.smoothScrollToPosition(chatMessages.size() - 1);
    }
    
    private void processBotResponse(String userMessage) {
        String response = "";
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("send money") || lowerMessage.contains("transfer")) {
            response = "To send money:\n" +
                    "1. Click on 'Send Money' button\n" +
                    "2. Enter recipient's mobile number or email ID (example@OnePay.com)\n" +
                    "3. Enter amount\n" +
                    "4. Click 'Send Money'\n\n" +
                    "You can send money using:\n" +
                    "- Mobile number (10 digits)\n" +
                    "- Email ID (7-digit key@OnePay.com)";
        } else if (lowerMessage.contains("receive money") || lowerMessage.contains("receive")) {
            response = "To receive money:\n" +
                    "1. Share your mobile number or email ID\n" +
                    "2. Or generate and share your QR code\n" +
                    "3. Money will be credited to your account instantly\n\n" +
                    "Your details:\n" +
                    "- Mobile: