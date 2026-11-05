package by.rowing.sanbaiteam.training.presentation.add

/** Куда форме нужно прокрутиться: к спортсмену или к конкретной карточке отрезка. */
internal sealed interface AddTrainingScrollTarget {

    data object Athlete : AddTrainingScrollTarget

    data class Piece(val pieceLocalId: Long) : AddTrainingScrollTarget
}
