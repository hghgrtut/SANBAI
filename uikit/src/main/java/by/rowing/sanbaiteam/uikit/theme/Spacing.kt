@file:Suppress(
    "unused",
    "UnusedReceiverParameter",
    "ObjectPropertyName",
    "MemberVisibilityCanBePrivate",
)

package by.rowing.sanbaiteam.uikit.theme

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

object Spacing {
    /** 0.dp */
    val None = 0.dp

    /** 2.dp */
    val _2XS = 2.dp

    /** 4.dp */
    val XS = 4.dp

    /** 8.dp */
    val S = 8.dp

    /** 12.dp */
    val SM = 12.dp

    /** 16.dp */
    val M = 16.dp

    /** 20.dp */
    val ML = 20.dp

    /** 24.dp */
    val L = 24.dp

    /** 32.dp */
    val XL = 32.dp

    /** 48.dp */
    val _3XL = 48.dp

    /** 64.dp */
    val _4XL = 64.dp

    /** 80.dp */
    val _5XL = 80.dp

    /** 2.dp */
    @Composable
    fun ColumnScope.Spacer2XS() = Spacer(modifier = Modifier.height(_2XS))

    /** 4.dp */
    @Composable
    fun ColumnScope.SpacerXS() = Spacer(modifier = Modifier.height(XS))

    /** 8.dp */
    @Composable
    fun ColumnScope.SpacerS() = Spacer(modifier = Modifier.height(S))

    /** 12.dp */
    @Composable
    fun ColumnScope.SpacerSM() = Spacer(modifier = Modifier.height(SM))

    /** 16.dp */
    @Composable
    fun ColumnScope.SpacerM() = Spacer(modifier = Modifier.height(M))

    /** 20.dp */
    @Composable
    fun ColumnScope.SpacerML() = Spacer(modifier = Modifier.height(ML))

    /** 24.dp */
    @Composable
    fun ColumnScope.SpacerL() = Spacer(modifier = Modifier.height(L))

    /** 32.dp */
    @Composable
    fun ColumnScope.SpacerXL() = Spacer(modifier = Modifier.height(XL))

    /** 48.dp */
    @Composable
    fun ColumnScope.Spacer3XL() = Spacer(modifier = Modifier.height(_3XL))

    /** 64.dp */
    @Composable
    fun ColumnScope.Spacer4XL() = Spacer(modifier = Modifier.height(_4XL))

    /** 80.dp */
    @Composable
    fun ColumnScope.Spacer5XL() = Spacer(modifier = Modifier.height(_5XL))

    /** 2.dp */
    @Composable
    fun RowScope.Spacer2XS() = Spacer(modifier = Modifier.width(_2XS))

    /** 4.dp */
    @Composable
    fun RowScope.SpacerXS() = Spacer(modifier = Modifier.width(XS))

    /** 8.dp */
    @Composable
    fun RowScope.SpacerS() = Spacer(modifier = Modifier.width(S))

    /** 12.dp */
    @Composable
    fun RowScope.SpacerSM() = Spacer(modifier = Modifier.width(SM))

    /** 16.dp */
    @Composable
    fun RowScope.SpacerM() = Spacer(modifier = Modifier.width(M))

    /** 20.dp */
    @Composable
    fun RowScope.SpacerML() = Spacer(modifier = Modifier.width(ML))

    /** 24.dp */
    @Composable
    fun RowScope.SpacerL() = Spacer(modifier = Modifier.width(L))

    /** 32.dp */
    @Composable
    fun RowScope.SpacerXL() = Spacer(modifier = Modifier.width(XL))

    /** 48.dp */
    @Composable
    fun RowScope.Spacer3XL() = Spacer(modifier = Modifier.width(_3XL))

    /** 64.dp */
    @Composable
    fun RowScope.Spacer4XL() = Spacer(modifier = Modifier.width(_4XL))

    /** 80.dp */
    @Composable
    fun RowScope.Spacer5XL() = Spacer(modifier = Modifier.width(_5XL))
}