package com.fgames.swiper.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.fgames.swiper.TheApplication
import com.fgames.swiper.model.state.MainViewModelState
import com.fgames.swiper.model.picture.PictureLoadRequest
import com.fgames.swiper.model.picture.PictureLoadResponse
import com.fgames.swiper.model.picture.PictureModel
import com.fgames.swiper.repository.MainStateRepository
import com.fgames.swiper.repository.PicsumPictureRepository
import com.fgames.swiper.utils.GradientGenerator
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlin.random.Random

class MainViewModel : ViewModel() {
    private val repository = MainStateRepository()

    private var _pictures: MutableList<PictureModel> = mutableListOf()
    private var _loadedPicture: MutableLiveData<PictureLoadResponse> = MutableLiveData()
    private var _prevAvailable: MutableLiveData<Boolean> = MutableLiveData()
    private var _updateFlag: MutableLiveData<Int> = MutableLiveData()

    private var picturesLoadingDisposable: Disposable? = null
    private var pictureLoadingDisposable: Disposable? = null

    private var page = 1
    private var index = 0

    private var lastLoadedPictureSize: Int = 0

    private var doOnPicturesLoad: (() -> Unit)? = null

    init {
        _updateFlag.value = INITIAL_FLAG

        repository.get().run {
            _pictures = pictures as MutableList<PictureModel>
            this@MainViewModel.page = page
            this@MainViewModel.index = index
        }

        _prevAvailable.value = index > 0
        initPicture()
    }

    fun getUpdateFlag(): LiveData<Int> = _updateFlag
    fun getLoadedPicture(): LiveData<PictureLoadResponse> = _loadedPicture
    fun getPrevAvailable(): LiveData<Boolean> = _prevAvailable

    private fun initPicture() {
        if (_pictures.size <= index) {
            doOnPicturesLoad = { initPicture() }
            loadPictures()
            return
        }

        loadPicture(index)
    }

    fun nextPicture() {
        _updateFlag.value = _updateFlag.value!!.plus(1)

        if (_pictures.size <= index + 1) {
            doOnPicturesLoad = { nextPicture() }
            loadPictures()
            return
        } else {
            setState { index++ }
        }

        loadPicture(index)
    }

    fun prevPicture() {
        _updateFlag.value = _updateFlag.value!!.plus(1)

        if (index > 0) {
            loadPicture(index - 1) {
                setState { index-- }
            }
        }
    }

    fun refresh() {
        _loadedPicture.value?.let {
            if(it.size == DEFAULT_PICTURE_SIZE) {
                _loadedPicture.value = PictureLoadResponse.refreshCopy(it)
            }
        }
    }

    private fun loadPicture(index: Int, doAfterLoad: (() -> Unit)? = null) {
        val pictureModel = _pictures[index]

        pictureLoadingDisposable?.dispose()
        pictureLoadingDisposable = Observable
            .just(
                PictureLoadRequest(pictureModel, SMALL_PICTURE_SIZE, 5),
                PictureLoadRequest(pictureModel, DEFAULT_PICTURE_SIZE)
            )
            .flatMap { request ->
                if (pictureModel.url.isEmpty()) {
                    Single.just(request)
                        .flatMap { Single.just(GradientGenerator.getGradient(request.pictureModel.id, request.size)) }
                        .map { picture -> PictureLoadResponse(picture, request.size) }
                        .toObservable()
                } else {
                    PicsumPictureRepository.loadPicture(request)
                        .onErrorReturn { GradientGenerator.getGradient(request.pictureModel.id, request.size) }
                        .map { picture -> PictureLoadResponse(picture, request.size) }
                        .toObservable()
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doAfterNext { response -> lastLoadedPictureSize = response.size }
            .doOnComplete { lastLoadedPictureSize = 0 }
            .filter { it.size > lastLoadedPictureSize }
            .subscribe(
                { response ->
                    _loadedPicture.value = response
                    if (response.size == DEFAULT_PICTURE_SIZE) doAfterLoad?.invoke()
                }, { t ->
                    t.printStackTrace()
                }
            )
    }

    private fun loadPictures() {
        TheApplication.picsumApi?.let {
            picturesLoadingDisposable?.dispose()
            picturesLoadingDisposable = it.getPictures(page)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterSuccess(::loadPicturesSuccess)
                .subscribe(
                    { pictures -> _pictures.addAll(pictures) },
                    {
                        _pictures.add(PictureModel(Random.nextInt(), ""))
                        loadPicturesFailed()
                    }
                )
        }
    }

    private fun loadPicturesSuccess(pictures: List<PictureModel>) {
        doOnPicturesLoad?.run {
            invoke()
            doOnPicturesLoad = null
        }
        setState { page++ }
    }

    private fun loadPicturesFailed() {
        doOnPicturesLoad?.run {
            invoke()
            doOnPicturesLoad = null
        }
    }

    private fun setState(newStateSetter: () -> Unit) {
        newStateSetter()
        _prevAvailable.value = index > 0
        repository.save(MainViewModelState(_pictures, page, index))
    }

    companion object {
        const val INITIAL_FLAG = 0

        const val SMALL_PICTURE_SIZE = 20
        const val DEFAULT_PICTURE_SIZE = 1000
    }
}