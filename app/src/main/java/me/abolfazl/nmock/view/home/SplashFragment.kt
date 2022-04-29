package me.abolfazl.nmock.view.home

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import me.abolfazl.nmock.R
import me.abolfazl.nmock.databinding.FragmentSplashBinding

class SplashFragment : Fragment() {

//    companion object {
//        private const val LOCATION_REQUEST = 1005
//    }

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
            Handler(Looper.getMainLooper())
                .postDelayed({
                    findNavController().navigate(R.id.action_splashFragment_to_homeFragment)
                }, 3000)
    }

//    private fun permissionProcess() {
//        val permission = Manifest.permission.ACCESS_FINE_LOCATION
//        val shouldShowRational =
//            ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)
//        if (!shouldShowRational) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                PermissionManager.getPermissionList().toTypedArray(),
//                LOCATION_REQUEST
//            )
//            return
//        }
//        Snackbar.make(
//            binding.root,
//            R.string.requestLocationPermissionRelational,
//            Snackbar.LENGTH_INDEFINITE
//        ).setAction(R.string.iAccept) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                PermissionManager.getPermissionList().toTypedArray(),
//                LOCATION_REQUEST
//            )
//        }.show()
//    }
}