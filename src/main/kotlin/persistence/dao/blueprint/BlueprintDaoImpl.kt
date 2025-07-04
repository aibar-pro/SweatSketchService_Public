package pro.aibar.sweatsketch.persistence.dao.workout

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import pro.aibar.sweatsketch.api.common.ApiConstants
import pro.aibar.sweatsketch.api.workout.*
import pro.aibar.sweatsketch.persistence.entity.workout.toDistanceActionDto
import pro.aibar.sweatsketch.persistence.entity.workout.toRepsActionDto
import pro.aibar.sweatsketch.persistence.entity.workout.toTimedActionDto
import pro.aibar.sweatsketch.persistence.table.actiontemplates.*
import pro.aibar.sweatsketch.persistence.table.workout.ExerciseBlueprintsTable
import pro.aibar.sweatsketch.persistence.table.workout.WorkoutBlueprintsTable
import java.util.*

class BlueprintDaoImpl : BlueprintDao {

    override fun saveBlueprint(
        blueprint: WorkoutBlueprintDto,
        visibility: BlueprintVisibility,
        authorId: UUID
    ): UUID = transaction {
        val templateId = blueprint.templateId?.let(UUID::fromString) ?: UUID.randomUUID()

        WorkoutBlueprintsTable.deleteWhere { WorkoutBlueprintsTable.id eq templateId }

        WorkoutBlueprintsTable.insert {
            it[id] = templateId
            it[name] = blueprint.name
            it[this.authorId] = authorId
            it[WorkoutBlueprintsTable.visibility] = visibility
            it[this.defaultRest] = blueprint.defaultRest
        }

        blueprint.exercises.forEach { ex ->
            val exId = UUID.randomUUID()

            ExerciseBlueprintsTable.insert {
                it[id] = exId
                it[workoutBlueprint] = templateId
                it[name] = ex.name
                it[position] = ex.position
                it[superSets] = ex.superSets
                it[this.internalRest] = ex.internalRest
                it[this.restAfter] = ex.restAfter
            }

            ex.actions.forEach { act ->
                when (act) {
                    is ActionBlueprintDto.Reps -> RepsActionBlueprintsTable.insert {
                        it[id] = UUID.randomUUID()
                        it[exerciseBlueprint]= exId
                        it[position] = act.position
                        it[sets] = act.sets
                        it[min] = act.min
                        it[max] = act.max
                        it[isMax] = act.isMax
                    }
                    is ActionBlueprintDto.Timed -> TimedActionBlueprintsTable.insert {
                        it[id] = UUID.randomUUID()
                        it[exerciseBlueprint]= exId
                        it[position] = act.position
                        it[sets] = act.sets
                        it[min] = act.min
                        it[max] = act.max
                        it[isMax] = act.isMax
                    }
                    is ActionBlueprintDto.Distance -> DistanceActionBlueprintsTable.insert {
                        it[id] = UUID.randomUUID()
                        it[exerciseBlueprint]= exId
                        it[position] = act.position
                        it[sets] = act.sets
                        it[min] = act.min
                        it[max] = act.max
                        it[unit] = act.unit
                        it[isMax] = act.isMax
                    }
                }
            }
        }
        templateId
    }

    override fun listTemplates(
        visibility: BlueprintVisibility?,
        authorId: UUID?
    ): List<BlueprintHeaderDto> = transaction {
        var query: Query = WorkoutBlueprintsTable.selectAll()
        visibility?.let { v -> query = query.orWhere { WorkoutBlueprintsTable.visibility eq BlueprintVisibility.PUBLIC } }
        authorId?.let { a -> query = query.orWhere { WorkoutBlueprintsTable.authorId eq a } }

        query.map {
            BlueprintHeaderDto(
                uuid = it[WorkoutBlueprintsTable.id].value.toString(),
                name = it[WorkoutBlueprintsTable.name],
                authorId = it[WorkoutBlueprintsTable.authorId].toString(),
                visibility = it[WorkoutBlueprintsTable.visibility]
            )
        }
    }

    override fun fetchBlueprint(id: UUID): WorkoutBlueprintDto? = transaction {
        val wRow = WorkoutBlueprintsTable.selectAll().where { WorkoutBlueprintsTable.id eq id }.singleOrNull()
            ?: return@transaction null

        val exercises = ExerciseBlueprintsTable
            .selectAll().where { ExerciseBlueprintsTable.workoutBlueprint eq id }
            .map { exRow ->
                val exId = exRow[ExerciseBlueprintsTable.id].value

                val reps = RepsActionBlueprintsTable.selectAll().where { RepsActionBlueprintsTable.exerciseBlueprint eq exId }
                    .map { it.toRepsActionDto() }
                val timed = TimedActionBlueprintsTable.selectAll().where { TimedActionBlueprintsTable.exerciseBlueprint eq exId }
                    .map { it.toTimedActionDto() }
                val dist = DistanceActionBlueprintsTable.selectAll().where { DistanceActionBlueprintsTable.exerciseBlueprint eq exId }
                    .map { it.toDistanceActionDto() }

                ExerciseBlueprintDto(
                    position = exRow[ExerciseBlueprintsTable.position],
                    name = exRow[ExerciseBlueprintsTable.name],
                    superSets = exRow[ExerciseBlueprintsTable.superSets],
                    actions = reps + timed + dist,
                    internalRest = exRow[ExerciseBlueprintsTable.internalRest],
                    restAfter = exRow[ExerciseBlueprintsTable.restAfter]
                )
            }

        WorkoutBlueprintDto(
            schemaVersion = ApiConstants.WORKOUT_SCHEMA_VERSION,
            templateId = id.toString(),
            name = wRow[WorkoutBlueprintsTable.name],
            exercises = exercises,
            defaultRest = wRow[WorkoutBlueprintsTable.defaultRest]
        )
    }
}