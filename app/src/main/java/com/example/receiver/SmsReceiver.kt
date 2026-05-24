package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import android.util.Log
import com.example.data.AppDatabase
import com.example.data.Expense
import com.example.data.SmsTransactionParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (sms in messages) {
                    val body = sms.messageBody ?: continue
                    val timestamp = sms.timestampMillis
                    
                    Log.d("SmsReceiver", "Received SMS: $body")
                    val parsed = SmsTransactionParser.parseMessage(body, timestamp)
                    if (parsed != null) {
                        val db = AppDatabase.getDatabase(context.applicationContext)
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                db.expenseDao().insertExpense(
                                    Expense(
                                        title = parsed.note,
                                        amount = parsed.amount,
                                        category = parsed.category,
                                        dateInMillis = parsed.timestamp,
                                        note = parsed.rawBody.take(150), // store snippet
                                        isIncome = parsed.isIncome
                                    )
                                )
                            } catch (e: Exception) {
                                Log.e("SmsReceiver", "Error inserting parsed SMS into database", e)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Error in onReceive broadcast handling", e)
        }
    }
}
