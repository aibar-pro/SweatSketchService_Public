package pro.aibar.sweatsketch.persistence.dao.workout

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import pro.aibar.sweatsketch.api.workout.*
import pro.aibar.sweatsketch.persistence.entity.workout.toDistanceActionDto
import pro.aibar.sweatsketch.persistence.entity.workout.toRepsActionDto
import pro.aibar.sweatsketch.persistence.entity.workout.toRestTimeTemplateDto
import pro.aibar.sweatsketch.persistence.entity.workout.toRestActionDto
import pro.aibar.sweatsketch.persistence.entity.workout.toTimedActionDto
import pro.aibar.sweatsketch.persistence.table.actiontemplates.*
import pro.aibar.sweatsketch.persistence.table.workout.ExerciseTemplatesTable
import pro.aibar.sweatsketch.persistence.table.workout.RestTimeTemplatesTable
import pro.aibar.sweatsketch.persistence.table.workout.WorkoutTemplatesTable
import java.util.*

class TemplateDaoImpl : TemplateDao {

    override fun createTemplate(body: NewTemplateRequestDto, authorId: UUID): UUID = transaction {
        val templateId = UUID.randomUUID()
        WorkoutTemplatesTable.insert {
            it[id] = templateId
            it[name] = body.workout.name
            it[this.authorId] = authorId
            it[visibility] = body.visibility
//            it[createdAt]  = Instant.now()
        }

        body.workout.exercises.forEach { ex ->
            val exId = UUID.randomUUID()
            ExerciseTemplatesTable.insert {
                it[id] = exId
                it[workoutTemplate] = templateId
                it[name] = ex.name
                it[position] = ex.position
                it[superSets] = ex.superSets
            }
            ex.actions.forEach { act ->
                when (act) {
                    is ActionTemplateDto.Reps -> RepsActionTemplatesTable.insert {
                        it[id] = UUID.randomUUID()
                        it[exerciseTemplate]= exId
                        it[position] = act.position
                        it[sets] = act.sets
                        it[min] = act.min
                        it[max] = act.max
                        it[isMax] = act.isMax
                    }
                    is ActionTemplateDto.Timed -> TimedActionTemplatesTable.insert {
                        it[id] = UUID.randomUUID()
                        it[exerciseTemplate]= exId
                        it[position] = act.position
                        it[sets] = act.sets
                        it[min] = act.min
                        it[max] = act.max
                        it[isMax] = act.isMax
                    }
                    is ActionTemplateDto.Distance -> DistanceActionTemplatesTable.insert {
                        it[id] = UUID.randomUUID()
                        it[exerciseTemplate]= exId
                        it[position] = act.position
                        it[sets] = act.sets
                        it[min] = act.min
                        it[max] = act.max
                        it[unit] = act.unit
                        it[isMax] = act.isMax
                    }
                    is ActionTemplateDto.Rest -> RestActionTemplatesTable.insert {
                        it[id] = UUID.randomUUID()
                        it[exerciseTemplate]= exId
                        it[position] = act.position
                        it[duration] = act.duration
                    }
                }
            }
        }
        templateId
    }

    override fun listTemplates(
        visibility: TemplateVisibility?,
        userId: UUID
    ): List<TemplateHeaderDto> = transaction {
        var query: Query = WorkoutTemplatesTable.selectAll()
        visibility?.let { v -> query = query.orWhere { WorkoutTemplatesTable.visibility eq TemplateVisibility.PUBLIC } }
        userId.let { a -> query = query.orWhere { WorkoutTemplatesTable.authorId eq a } }

        query.map {
            TemplateHeaderDto(
                uuid = it[WorkoutTemplatesTable.id].value.toString(),
                name = it[WorkoutTemplatesTable.name],
                authorId = it[WorkoutTemplatesTable.authorId].toString(),
                visibility = it[WorkoutTemplatesTable.visibility]
            )
        }
    }

    override fun getTemplate(id: UUID): WorkoutTemplateDto? = transaction {
        val wRow = WorkoutTemplatesTable.selectAll().where { WorkoutTemplatesTable.id eq id }.singleOrNull()
            ?: return@transaction null

        val exercises = ExerciseTemplatesTable
            .selectAll().where { ExerciseTemplatesTable.workoutTemplate eq id }
            .map { exRow ->
                val exId = exRow[ExerciseTemplatesTable.id].value

                val reps = RepsActionTemplatesTable.selectAll().where { RepsActionTemplatesTable.exerciseTemplate eq exId }
                    .map { it.toRepsActionDto() }
                val timed = TimedActionTemplatesTable.selectAll().where { TimedActionTemplatesTable.exerciseTemplate eq exId }
                    .map { it.toTimedActionDto() }
                val dist = DistanceActionTemplatesTable.selectAll().where { DistanceActionTemplatesTable.exerciseTemplate eq exId }
                    .map { it.toDistanceActionDto() }
                val rest = RestActionTemplatesTable.selectAll().where { RestActionTemplatesTable.exerciseTemplate eq exId }
                    .map { it.toRestActionDto() }

                ExerciseTemplateDto(
                    uuid = exId.toString(),
                    name = exRow[ExerciseTemplatesTable.name],
                    superSets = exRow[ExerciseTemplatesTable.superSets],
                    position = exRow[ExerciseTemplatesTable.position],
                    actions = reps + timed + dist + rest
                )
            }

        val restBetween = RestTimeTemplatesTable
            .selectAll().where { RestTimeTemplatesTable.workoutTemplate eq id }
            .map { it.toRestTimeTemplateDto() }

        WorkoutTemplateDto(
            uuid = id.toString(),
            name = wRow[WorkoutTemplatesTable.name],
            exercises = exercises,
            restBetween = restBetween
        )
    }

    override fun cloneTemplate(id: UUID, ownerId: UUID): UUID = transaction {
        val template = getTemplate(id) ?: error("template $id not found")
        val templateId = UUID.randomUUID()
        val newTemplate = NewTemplateRequestDto(template, TemplateVisibility.PRIVATE)
        createTemplate(newTemplate, ownerId)

        templateId
    }
}