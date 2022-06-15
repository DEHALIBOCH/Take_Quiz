package com.stdev.takequiz.presentation.ui

import android.os.Bundle
import android.text.Html
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.stdev.takequiz.R
import com.stdev.takequiz.data.model.Category
import com.stdev.takequiz.data.model.Question
import com.stdev.takequiz.data.model.Quiz
import com.stdev.takequiz.data.model.QuizResult
import com.stdev.takequiz.databinding.FragmentQuizBinding
import com.stdev.takequiz.presentation.viewmodel.QuizViewModel
import com.stdev.takequiz.presentation.viewmodel.QuizViewModelFactory
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuizFragment : Fragment(),View.OnClickListener{

    @Inject
    lateinit var factory: QuizViewModelFactory
    lateinit var viewModel : QuizViewModel
    private lateinit var binding : FragmentQuizBinding

    private lateinit var category : Category
    private var difficulty = "any"
    private var type = "multiple"
    private var questions = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        category = QuizFragmentArgs.fromBundle(requireArguments()).category
        difficulty = QuizFragmentArgs.fromBundle(requireArguments()).difficulty
        type = QuizFragmentArgs.fromBundle(requireArguments()).type
        questions = QuizFragmentArgs.fromBundle(requireArguments()).questions.toInt()


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_quiz, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentQuizBinding.bind(view)
        viewModel = ViewModelProvider(this, factory)[QuizViewModel::class.java]

        binding.quizOptionOne.setOnClickListener(this)
        binding.quizOptionTwo.setOnClickListener(this)
        binding.quizOptionThree.setOnClickListener(this)
        binding.quizOptionFour.setOnClickListener(this)
        binding.quizNextButton.setOnClickListener(this)

        viewModelShits()

    }

    private fun viewModelShits(){
        viewModel.status.observe(viewLifecycleOwner) {
            if (!it) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Error")
                    .setMessage("There aren't sufficient questions in the database. Try reducing the number of questions and try again. ): ")
                    .setCancelable(false)
                    .setPositiveButton("Okay") { _, _ ->
                        findNavController().navigate(R.id.action_quizFragment_to_homeFragment)
                    }
                    .show()
            }
        }

        viewModel.getQuiz(questions, category.id, difficulty, type)

        viewModel.currentQuiz.observe(viewLifecycleOwner) {
            clear()
            binding.quizQuestionText.text = HtmlCompat.fromHtml(it.question,HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.quizOptionOne.text = HtmlCompat.fromHtml(it.optionOne,HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.quizOptionTwo.text = HtmlCompat.fromHtml(it.optionTwo,HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.quizOptionThree.text = HtmlCompat.fromHtml(it.optionThree,HtmlCompat.FROM_HTML_MODE_LEGACY)
            binding.quizOptionFour.text = HtmlCompat.fromHtml(it.optionFour,HtmlCompat.FROM_HTML_MODE_LEGACY)
        }

        viewModel.currentQuestion.observe(viewLifecycleOwner) {
            binding.quizQuestionNo.text = "Question $it/$questions"
        }

        viewModel.buttonText.observe(viewLifecycleOwner) {
            binding.quizNextButton.text = it
        }

        viewModel.movetonext.observe(viewLifecycleOwner) {
            binding.quizNextButton.isEnabled = it
        }

        viewModel.quizType.observe(viewLifecycleOwner){
            if (!it){
                binding.quizOptionThree.visibility = View.INVISIBLE
                binding.quizOptionFour.visibility = View.INVISIBLE
            }
        }
    }

    private fun clear(){
        //clearing the fields
        binding.quizOptionOne.background = ContextCompat.getDrawable(requireContext(),R.drawable.option_unselected)
        binding.quizOptionTwo.background = ContextCompat.getDrawable(requireContext(),R.drawable.option_unselected)
        binding.quizOptionThree.background = ContextCompat.getDrawable(requireContext(),R.drawable.option_unselected)
        binding.quizOptionFour.background = ContextCompat.getDrawable(requireContext(),R.drawable.option_unselected)
    }

    private fun showResult(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Your Result")
            .setMessage("You answered ${viewModel.correctAnswers.value} question(s) correctly out of $questions questions. \nKeep it up")
            .setPositiveButton("Try again"){_,_->
                viewModel.startAgain()
                viewModelShits()
            }.setNegativeButton("Exit"){_,_->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }

    override fun onClick(view: View?) {
        when(view?.id){
            binding.quizNextButton.id -> {
                if (viewModel.currentQuestion.value == questions){
                    viewModel.endQuiz()
                    showResult()
                    //Toast.makeText(requireContext(),"Correct answers ${viewModel.correctAnswers.value}",Toast.LENGTH_SHORT).show()
                }else{
                    viewModel.nextQuestion()
                }
            }
            binding.quizOptionOne.id -> {
                viewModel.verityAnswer(1,binding.quizOptionOne,requireContext(),binding.quizOptionOne,binding.quizOptionTwo,binding.quizOptionThree,binding.quizOptionFour)
            }
            binding.quizOptionTwo.id -> {
                viewModel.verityAnswer(2,binding.quizOptionTwo,requireContext(),binding.quizOptionOne,binding.quizOptionTwo,binding.quizOptionThree,binding.quizOptionFour)
            }
            binding.quizOptionThree.id -> {
                viewModel.verityAnswer(3,binding.quizOptionThree,requireContext(),binding.quizOptionOne,binding.quizOptionTwo,binding.quizOptionThree,binding.quizOptionFour)

            }
            binding.quizOptionFour.id -> {
                viewModel.verityAnswer(4,binding.quizOptionFour,requireContext(),binding.quizOptionOne,binding.quizOptionTwo,binding.quizOptionThree,binding.quizOptionFour)
            }
        }
    }

}