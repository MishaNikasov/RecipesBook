package com.my.project.firstkotlin.ui.fragment.search

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.my.project.firstkotlin.data.remote.data.repository.RemoteRecipeRepository
import com.my.project.firstkotlin.data.remote.util.Resource
import com.my.project.firstkotlin.data.remote.data.response.RecipeResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class SearchRecipeViewModel @ViewModelInject constructor (
    private val remoteRecipeRepository : RemoteRecipeRepository
) : ViewModel() {

    //remote
    val searchRecipesList : MutableLiveData<Resource<RecipeResponse>> = MutableLiveData()
    private var searchRecipesResponse : RecipeResponse? = null

    //filter
    var type : String = ""

    fun searchRecipes (searchRecipes : String = "") = viewModelScope.launch {

        if (searchRecipesResponse != null)
            searchRecipesList.postValue(Resource.Loading(false))
        else
            searchRecipesList.postValue(Resource.Loading(true))

        val response =
            if (searchRecipesResponse != null)
                remoteRecipeRepository.getRecipeResult(searchRecipes, searchRecipesResponse?.recipes!!.size, type)
            else
                remoteRecipeRepository.getRecipeResult(searchRecipes, 0, type)

        searchRecipesList.postValue(handleSearchRecipeResponse(response))
    }

    private fun handleSearchRecipeResponse(response: Response<RecipeResponse>) : Resource<RecipeResponse> {
        if (response.isSuccessful) {
            response.body()?.let {
                if (searchRecipesResponse == null) {
                    searchRecipesResponse = it
                } else {
                    val oldRecipes = searchRecipesResponse?.recipes
                    val newRecipes = it.recipes
                    oldRecipes?.addAll(newRecipes)
                }
                return Resource.Success(searchRecipesResponse ?: it)
            }
        }
        return Resource.Error("Something wrong with search: " + response.message())
    }

    fun cleanCurrentList() {
        searchRecipesResponse = null
    }
}