package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PartyEntity
import com.example.ui.theme.AppText
import com.example.ui.theme.LocalNanoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OptionalMillerField(
    parties: List<PartyEntity>,
    selectedMiller: PartyEntity?,
    onMillerSelected: (PartyEntity?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalNanoColors.current
    val mills = parties.filter { it.type == "SUPPLIER" || it.type == "BOTH" }
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedMiller?.name ?: "কোনো মিলার নয়",
                onValueChange = {},
                readOnly = true,
                label = { Text("মিলার (ঐচ্ছিক)", color = colors.textMuted) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedBorderColor = colors.primary,
                    unfocusedBorderColor = colors.border
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("কোনো মিলার নয়") },
                    onClick = {
                        onMillerSelected(null)
                        expanded = false
                    }
                )
                mills.forEach { mill ->
                    DropdownMenuItem(
                        text = { Text(mill.name) },
                        onClick = {
                            onMillerSelected(mill)
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "খালি রাখলে শুধু বস্তা যোগ হবে। মিলারের বাকি ও ক্যাশ বদলাবে না।",
            fontSize = AppText.Caption,
            color = colors.textMuted
        )
    }
}
