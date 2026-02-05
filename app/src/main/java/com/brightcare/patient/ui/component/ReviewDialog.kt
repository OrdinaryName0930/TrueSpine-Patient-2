package com.brightcare.patient.ui.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.brightcare.patient.ui.theme.*

/**
 * Review Dialog for rating chiropractors after completed appointments
 * Dialog para sa pag-rate ng chiropractors pagkatapos ng completed appointments
 */
@Composable
fun ReviewDialog(
    chiropractorName: String,
    onDismiss: () -> Unit,
    onSubmit: (rating: Int, comment: String, isAnonymous: Boolean) -> Unit,
    isSubmitting: Boolean = false
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var isAnonymous by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = { if (!isSubmitting) onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { if (!isSubmitting) onDismiss() },
                        enabled = !isSubmitting
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Gray500
                        )
                    }
                }

                // Star icon
                Surface(
                    modifier = Modifier.size(72.dp),
                    shape = RoundedCornerShape(36.dp),
                    color = Orange50
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Review",
                        tint = Orange500,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = "Rate Your Experience",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gray900
                )

                // Subtitle with chiropractor name
                Text(
                    text = "How was your session with $chiropractorName?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                // Tagalog subtitle
                Text(
                    text = "Paano ang iyong session kasama si $chiropractorName?",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Star Rating
                StarRatingSelector(
                    rating = rating,
                    onRatingChanged = { 
                        rating = it
                        showError = false
                    }
                )

                // Rating description
                Text(
                    text = getRatingDescription(rating),
                    style = MaterialTheme.typography.bodyMedium,
                    color = getRatingColor(rating),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 8.dp)
                )

                // Error message if no rating
                if (showError && rating == 0) {
                    Text(
                        text = "Please select a rating / Pumili ng rating",
                        style = MaterialTheme.typography.bodySmall,
                        color = Error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Comment text field
                OutlinedTextField(
                    value = comment,
                    onValueChange = { if (it.length <= 500) comment = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Share your experience (optional)") },
                    placeholder = { Text("Tell us about your session...") },
                    minLines = 3,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Blue500,
                        unfocusedBorderColor = Gray300,
                        focusedLabelColor = Blue500
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSubmitting,
                    supportingText = {
                        Text(
                            text = "${comment.length}/500 characters",
                            color = Gray500,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Anonymous checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = !isSubmitting) { isAnonymous = !isAnonymous }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isAnonymous,
                        onCheckedChange = { isAnonymous = it },
                        enabled = !isSubmitting,
                        colors = CheckboxDefaults.colors(
                            checkedColor = Blue500,
                            uncheckedColor = Gray400
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Post anonymously",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gray800
                        )
                        Text(
                            text = "Your name will not be shown / Hindi ipapakita ang pangalan mo",
                            style = MaterialTheme.typography.bodySmall,
                            color = Gray500
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Submit button
                Button(
                    onClick = {
                        if (rating == 0) {
                            showError = true
                        } else {
                            onSubmit(rating, comment, isAnonymous)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Blue500,
                        disabledContainerColor = Gray300
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSubmitting && rating > 0
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submitting...",
                            style = MaterialTheme.typography.labelLarge,
                            color = White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.RateReview,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Submit Review",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Cancel button
                TextButton(
                    onClick = { if (!isSubmitting) onDismiss() },
                    modifier = Modifier.padding(top = 8.dp),
                    enabled = !isSubmitting
                ) {
                    Text(
                        text = "Maybe Later",
                        color = Gray600
                    )
                }
            }
        }
    }
}

/**
 * Star rating selector component
 * Component para sa pagpili ng star rating
 */
@Composable
private fun StarRatingSelector(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        (1..5).forEach { starIndex ->
            AnimatedStar(
                filled = starIndex <= rating,
                onClick = { onRatingChanged(starIndex) }
            )
        }
    }
}

/**
 * Animated star component with scale and color animation
 * Animated star component na may scale at color animation
 */
@Composable
private fun AnimatedStar(
    filled: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (filled) 1.2f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "star_scale"
    )
    
    val color by animateColorAsState(
        targetValue = if (filled) Orange500 else Gray300,
        animationSpec = tween(durationMillis = 200),
        label = "star_color"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier.scale(scale)
    ) {
        Icon(
            imageVector = if (filled) Icons.Default.Star else Icons.Default.StarBorder,
            contentDescription = "Star",
            tint = color,
            modifier = Modifier.size(40.dp)
        )
    }
}

/**
 * Get rating description based on rating value
 * Kunin ang description ng rating batay sa rating value
 */
private fun getRatingDescription(rating: Int): String {
    return when (rating) {
        1 -> "Poor / Hindi maganda"
        2 -> "Fair / Puwede na"
        3 -> "Good / Maganda"
        4 -> "Very Good / Napakaganda"
        5 -> "Excellent / Kahanga-hanga!"
        else -> "Tap a star to rate / Pindutin ang star para mag-rate"
    }
}

/**
 * Get rating color based on rating value
 * Kunin ang kulay ng rating batay sa rating value
 */
private fun getRatingColor(rating: Int): Color {
    return when (rating) {
        1 -> Red500
        2 -> Orange500
        3 -> Orange400
        4 -> Green500
        5 -> Green600
        else -> Gray500
    }
}

/**
 * Review Success Dialog shown after successful submission
 * Dialog ng tagumpay na ipinapakita pagkatapos ng matagumpay na submission
 */
@Composable
fun ReviewSuccessDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Success icon
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    color = Green50
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Green500,
                        modifier = Modifier
                            .padding(16.dp)
                            .size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Thank You!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Green600
                )

                Text(
                    text = "Salamat!",
                    style = MaterialTheme.typography.titleMedium,
                    color = Green500
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Your review has been submitted successfully. Your feedback helps us improve our services.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Gray600,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Ang iyong review ay matagumpay na naisumite. Ang iyong feedback ay nakakatulong sa pagpapabuti ng aming serbisyo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gray500,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Done",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

