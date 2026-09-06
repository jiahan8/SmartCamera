package com.jiahan.smartcamera.fake

import com.jiahan.smartcamera.data.repository.PhotoRepository
import com.jiahan.smartcamera.domain.Photo
import com.jiahan.smartcamera.domain.PhotoPage

/**
 * In-memory [PhotoRepository] test double, standing in for the Unsplash-backed implementation.
 *
 * The tenth fake, and the last repository interface to get one -- `ExploreViewModel` was the only
 * consumer and its unit test doubles the interface with mockk, so nothing had needed one. A
 * `sharedTest` screen suite does: mockk is on `testImplementation` alone, so a mock cannot follow
 * a suite into the androidTest source set.
 *
 * Browse and search results are configured separately, because the screen shows them in different
 * states and a test drives one without disturbing the other. [hasMore] is held per result rather
 * than derived from the list, matching the real repository -- pagination reads the row count the
 * source returned, never the mapped list size.
 */
class FakePhotoRepository : PhotoRepository {

    var listResult: Result<PhotoPage> = Result.success(PhotoPage(emptyList(), hasMore = false))
    var searchResult: Result<PhotoPage> = Result.success(PhotoPage(emptyList(), hasMore = false))

    var listCallCount = 0
    var searchCallCount = 0
    var lastListPage: Int? = null
    var lastSearchQuery: String? = null
    var lastSearchPage: Int? = null

    /** Stubs a successful browse page. `hasMore` drives pagination independently of list size. */
    fun setPhotos(photos: List<Photo>, hasMore: Boolean = false) {
        listResult = Result.success(PhotoPage(photos, hasMore))
    }

    /** Stubs a successful search page, the result [searchPhotos] returns for any query. */
    fun setSearchPhotos(photos: List<Photo>, hasMore: Boolean = false) {
        searchResult = Result.success(PhotoPage(photos, hasMore))
    }

    override suspend fun listPhotos(page: Int, pageSize: Int): Result<PhotoPage> {
        listCallCount++
        lastListPage = page
        return listResult
    }

    override suspend fun searchPhotos(
        query: String,
        page: Int,
        pageSize: Int
    ): Result<PhotoPage> {
        searchCallCount++
        lastSearchQuery = query
        lastSearchPage = page
        return searchResult
    }
}