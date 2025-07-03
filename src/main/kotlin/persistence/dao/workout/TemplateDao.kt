package pro.aibar.sweatsketch.persistence.dao.workout

import pro.aibar.sweatsketch.api.workout.NewTemplateRequestDto
import pro.aibar.sweatsketch.api.workout.TemplateHeaderDto
import pro.aibar.sweatsketch.api.workout.TemplateVisibility
import pro.aibar.sweatsketch.api.workout.WorkoutTemplateDto
import java.util.UUID

interface TemplateDao {
    fun createTemplate(body: NewTemplateRequestDto, authorId: UUID): UUID

    fun listTemplates(
        visibility: TemplateVisibility? = null,
        userId: UUID
    ): List<TemplateHeaderDto>

    fun getTemplate(id: UUID): WorkoutTemplateDto?

    fun cloneTemplate(id: UUID, ownerId: UUID): UUID
}