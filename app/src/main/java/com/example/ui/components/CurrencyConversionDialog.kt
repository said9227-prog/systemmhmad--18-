package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.ExchangeRate
import com.example.domain.currency.Currency
import com.example.domain.currency.CurrencyConversionService
import com.example.domain.currency.Money
import com.example.util.FormatUtils
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConversionDialog(
    rates: List<ExchangeRate>,
    initialAmount: Double = 100.0,
    initialFromCurrency: String = "الريال اليمني",
    initialToCurrency: String = "الريال السعودي",
    onDismiss: () -> Unit
) {
    var amountStr by remember { mutableStateOf(if (initialAmount > 0) initialAmount.toString() else "100") }
    var fromCurrency by remember { mutableStateOf(Currency.fromCodeOrName(initialFromCurrency)) }
    var toCurrency by remember { mutableStateOf(Currency.fromCodeOrName(initialToCurrency)) }
    var expandedFrom by remember { mutableStateOf(false) }
    var expandedTo by remember { mutableStateOf(false) }

    val rateMap = remember(rates) {
        rates.associate {
            (Currency.fromCodeOrName(it.fromCurrency) to Currency.fromCodeOrName(it.toCurrency)) to BigDecimal.valueOf(it.rate)
        }
    }

    val amountDouble = amountStr.toDoubleOrNull() ?: 0.0
    val money = Money(amountDouble, fromCurrency)
    val conversion = CurrencyConversionService.convert(money, toCurrency, rateMap)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CurrencyExchange,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = "مصارفة وحساب العملات",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Divider()

                // Amount Field
                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("المبلغ المراد تحويله") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Currency Choice Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // From Currency Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedFrom,
                        onExpandedChange = { expandedFrom = !expandedFrom },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = fromCurrency.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("من عملة") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFrom) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedFrom,
                            onDismissRequest = { expandedFrom = false }
                        ) {
                            Currency.values().forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text(curr.displayName) },
                                    onClick = {
                                        fromCurrency = curr
                                        expandedFrom = false
                                    }
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            val temp = fromCurrency
                            fromCurrency = toCurrency
                            toCurrency = temp
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = "تبديل",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // To Currency Dropdown
                    ExposedDropdownMenuBox(
                        expanded = expandedTo,
                        onExpandedChange = { expandedTo = !expandedTo },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = toCurrency.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("إلى عملة") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTo) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTo,
                            onDismissRequest = { expandedTo = false }
                        ) {
                            Currency.values().forEach { curr ->
                                DropdownMenuItem(
                                    text = { Text(curr.displayName) },
                                    onClick = {
                                        toCurrency = curr
                                        expandedTo = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Result Box
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "النتيجة بعد المصارفة",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "${FormatUtils.formatAmount(conversion.convertedMoney.toDouble())} ${toCurrency.code}",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = conversion.rateDescription,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Dismiss Button
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("إغلاق", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
