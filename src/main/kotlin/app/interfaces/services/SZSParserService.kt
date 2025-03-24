package app.interfaces.services

import entities.SZSModel
import kotlinx.coroutines.flow.Flow
import util.commandResult.IntermediateStatus
import util.commandResult.RootError
import java.io.BufferedReader

interface SZSParserService {
    fun parse(bufferedReader: BufferedReader): Flow<IntermediateStatus<SZSModel, RootError>>
}