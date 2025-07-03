package pro.aibar.sweatsketch.api.workout

import kotlinx.serialization.Serializable

@Serializable
enum class TemplateVisibility { PRIVATE, UNLISTED, PUBLIC }