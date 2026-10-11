package com.pulse.calculator.ui.history

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pulse.calculator.R
import com.pulse.calculator.state.HistoryEntry
import com.pulse.calculator.ui.TestTags
import com.pulse.calculator.ui.display.ExpressionFormat
import com.pulse.calculator.ui.theme.ClearContainer
import com.pulse.calculator.ui.theme.DisplayExpression
import com.pulse.calculator.ui.theme.DisplayPreview
import com.pulse.calculator.ui.theme.HistoryActionStyle
import com.pulse.calculator.ui.theme.HistoryExpressionStyle
import com.pulse.calculator.ui.theme.HistoryResultStyle
import com.pulse.calculator.ui.theme.HistoryRowEnterMillis
import com.pulse.calculator.ui.theme.HistoryRowRiseDistance
import com.pulse.calculator.ui.theme.HistoryRowStaggerMillis
import com.pulse.calculator.ui.theme.HistoryStaggerMaxRows
import com.pulse.calculator.ui.theme.HistoryTitleStyle
import com.pulse.calculator.ui.theme.LocalReducedMotion
import com.pulse.calculator.ui.theme.PulseTheme
import com.pulse.calculator.ui.theme.SheetContainer
import com.pulse.calculator.ui.theme.SheetDivider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val SheetPadding = 20.dp
private val EmptyStateHeight = 200.dp

/**
 * The session history as a bottom sheet. Tapping a row loads its expression right away and then
 * lets the sheet slide out; nothing here waits on an animation.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorySheet(
    history: List<HistoryEntry>,
    onLoad: (HistoryEntry) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SheetContainer,
        contentColor = DisplayExpression,
    ) {
        Column(modifier = Modifier.testTag(TestTags.HistorySheet)) {
            Header(canClear = history.isNotEmpty(), onClear = onClear)
            HorizontalDivider(color = SheetDivider)
            if (history.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    itemsIndexed(history, key = { _, entry -> entry.id }) { index, entry ->
                        HistoryRow(
                            entry = entry,
                            index = index,
                            onClick = {
                                onLoad(entry)
                                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
                            },
                        )
                        HorizontalDivider(color = SheetDivider)
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(canClear: Boolean, onClear: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = SheetPadding, end = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.history_title),
            style = HistoryTitleStyle,
            color = DisplayExpression,
        )
        if (canClear) {
            TextButton(onClick = onClear, modifier = Modifier.testTag(TestTags.HistoryClear)) {
                Text(
                    text = stringResource(R.string.history_clear),
                    style = HistoryActionStyle,
                    color = ClearContainer,
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = EmptyStateHeight)
            .padding(SheetPadding)
            .testTag(TestTags.HistoryEmpty),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.history_empty_title),
            style = HistoryTitleStyle,
            color = DisplayExpression,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.history_empty_hint),
            style = HistoryExpressionStyle,
            color = DisplayPreview,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry, index: Int, onClick: () -> Unit) {
    val expression = ExpressionFormat.toDisplay(entry.expression)
    val description = stringResource(R.string.history_row_description, expression, entry.result)

    // Only the first few rows stagger in; the animatable exists only for those.
    val animate = !LocalReducedMotion.current && index < HistoryStaggerMaxRows
    val enter = remember(entry.id) { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(entry.id) {
        if (animate) {
            delay(index * HistoryRowStaggerMillis.toLong())
            enter.animateTo(1f, tween(HistoryRowEnterMillis))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                alpha = enter.value
                translationY = HistoryRowRiseDistance.toPx() * (1f - enter.value)
            }
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = SheetPadding, vertical = 12.dp)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .testTag(TestTags.historyRow(index)),
    ) {
        Text(
            text = expression,
            style = HistoryExpressionStyle,
            color = DisplayPreview,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = entry.result,
            style = HistoryResultStyle,
            color = DisplayExpression,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(widthDp = 360)
@Composable
private fun HistoryRowPreview() {
    PulseTheme {
        Box { HistoryRow(entry = HistoryEntry(1, "12+5*(3+2)", "37"), index = 0, onClick = {}) }
    }
}
