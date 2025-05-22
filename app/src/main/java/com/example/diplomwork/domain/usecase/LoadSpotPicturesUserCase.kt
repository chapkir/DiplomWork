package com.example.diplomwork.domain.usecase

import com.example.diplomwork.data.repos.SpotRepository
import javax.inject.Inject

class LoadSpotPicturesUseCase @Inject constructor(
    private val spotRepository: SpotRepository
) {
    suspend operator fun invoke(spotId: Long, firstImage: String): List<String> {
        val response = spotRepository.getSpotPictures(spotId)

        return response.pictures
            .filterNotNull()
            .filterNot { it == firstImage }
    }
}