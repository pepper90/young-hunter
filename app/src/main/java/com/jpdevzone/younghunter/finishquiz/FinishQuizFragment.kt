package com.jpdevzone.younghunter.finishquiz

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.play.core.review.ReviewManagerFactory
import com.jpdevzone.younghunter.R
import com.jpdevzone.younghunter.databinding.FragmentFinishQuizBinding
import com.jpdevzone.younghunter.utils.setBackground
import com.jpdevzone.younghunter.utils.stringBuilder
import es.dmoral.toasty.Toasty

class FinishQuizFragment : Fragment() {
    private lateinit var binding : FragmentFinishQuizBinding
    private lateinit var viewModel: FinishQuizViewModel
    private lateinit var args: FinishQuizFragmentArgs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding  = DataBindingUtil.inflate(inflater,R.layout.fragment_finish_quiz, container, false)

        // Gets arguments from Bundle
        args = FinishQuizFragmentArgs.fromBundle(requireArguments())

        viewModel = ViewModelProvider(
            this,
            FinishQuizViewModelFactory(
                args.result,
                args.time,
                args.max
            )
        )[FinishQuizViewModel::class.java]

        binding.vm = viewModel

        binding.lifecycleOwner = viewLifecycleOwner

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Sets random background
        binding.finishQuizBackground.setImageResource(setBackground)

        // Sets back navigation
        binding.arrowBackIv.setOnClickListener {
            navigateBack()
        }

        // Create share intent with result on click
        binding.btnShare.setOnClickListener {
            val shareIntent = Intent().apply {
                this.action = Intent.ACTION_SEND
                this.putExtra(
                    Intent.EXTRA_TEXT,
                    stringBuilder(
                        binding.congratsTv.text.toString(),
                        binding.scoreTv.text.toString()
                    )
                )
                this.type = "text/plain"
            }
            startActivity(shareIntent) }

        // Copy result to clipboard on click
        binding.btnCopy.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("result",
                stringBuilder(
                    binding.congratsTv.text.toString(),
                    binding.scoreTv.text.toString()
                )
            )
            clipboard.setPrimaryClip(clip)
            Toasty.custom(requireContext(), R.string.copied, R.drawable.ic_copy, R.color.accent_gray, Toast.LENGTH_SHORT, true, true).show()
        }

        // Navigate to Dashboard on click
        binding.btnContinue.setOnClickListener {
            navigateBack()
        }

        // Checks device API & triggers in-app review flow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inAppReview()
        }
    }

    // Navigate to DashboardFragment
    private fun navigateBack() {
        this.findNavController().navigate(
            FinishQuizFragmentDirections.actionFinishQuizFragmentToDashboardFragment()
        )
    }

    // Handles in-app reviews
    private fun inAppReview() {
        val reviewManager = ReviewManagerFactory.create(requireContext())
        val requestReviewFlow = reviewManager.requestReviewFlow()
        requestReviewFlow.addOnCompleteListener { request ->
            if (request.isSuccessful) {
                val reviewInfo = request.result
                val flow = reviewManager.launchReviewFlow(requireActivity(), reviewInfo)
                flow.addOnCompleteListener {}
            }
        }
    }
}