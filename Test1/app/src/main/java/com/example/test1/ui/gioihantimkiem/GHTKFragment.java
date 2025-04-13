package com.unetikhoaluan2025.timabk.ui.gioihantimkiem;

import static android.content.ContentValues.TAG;
import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.unetikhoaluan2025.timabk.R;
import com.unetikhoaluan2025.timabk.data.AppDatabase;
import com.unetikhoaluan2025.timabk.databinding.FragmentGhtkBinding;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GHTKFragment extends Fragment {
   private FragmentGhtkBinding binding;
    private KeyDAO keyDAO;
   private KeyAdapter adapter;
   private GHTKViewModel viewModel;
   private Key selectedKey = null;
   private ExecutorService executor;
   private SharedPreferences unlockPrefs;
   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      binding = FragmentGhtkBinding.inflate(inflater, container, false);
      return binding.getRoot();
   }

   @SuppressLint("SetTextI18n")
   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      viewModel = new ViewModelProvider(this).get(GHTKViewModel.class);
      // Khởi tạo database & DAO
       AppDatabase keyDB = AppDatabase.getInstance(requireContext());
      keyDAO = keyDB.keyDAO();


      adapter = new KeyAdapter(keyDAO, key -> {
         selectedKey = key;
         binding.editTuKhoa.setText(key.getKeyword());
         binding.editTuKhoa.setEnabled(false);
         binding.btnSua.setText("Sửa");
      });

      binding.recyclerViewTK.setLayoutManager(new LinearLayoutManager(requireContext()));
      binding.recyclerViewTK.setAdapter(adapter);

      loadData();
      binding.btnHuy.setOnClickListener(v -> clickHuy());

      // Xử lý nút "Thêm"
      binding.btnThem.setOnClickListener(v -> {
         if (binding.btnThem.getText().toString().equals("Thêm"))
         {
            // Mở EditText và đổi nút thành "Lưu"
            binding.btnHuy.setVisibility(View.VISIBLE);
            binding.editTuKhoa.setEnabled(true);
            binding.editTuKhoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
            binding.editTuKhoa.requestFocus();
            binding.btnThem.setText("Lưu");
            binding.btnSua.setEnabled(false);
            binding.btnSua.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.silver));
            binding.btnXoa.setEnabled(false);
            binding.btnXoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.silver));
         } else {
            String keyword = binding.editTuKhoa.getText().toString().trim();
            if (keyword.isEmpty()) {
               Toast.makeText(requireContext(), "Vui lòng nhập từ khóa!", Toast.LENGTH_SHORT).show();
               return;
            }

            // Kiểm tra xem từ khóa đã tồn tại chưa
            Executors.newSingleThreadExecutor().execute(() -> {
               Key existingKey = keyDAO.getKeywordByWord(keyword);
               if (existingKey != null) {
                  // Từ khóa đã tồn tại
                  requireActivity().runOnUiThread(() -> {
                     Toast.makeText(requireContext(), "Từ khóa đã tồn tại!", Toast.LENGTH_SHORT).show();
                  });
               } else {
                  // Từ khóa chưa tồn tại, thực hiện thêm mới
                  keyDAO.insert(new Key(keyword));
                  requireActivity().runOnUiThread(() -> {
                     binding.editTuKhoa.setText("");
                     binding.editTuKhoa.setEnabled(false);
                     binding.editTuKhoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.silver));
                     binding.btnThem.setText("Thêm");
                     binding.btnSua.setEnabled(true);
                     binding.btnSua.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.teal));
                     binding.btnXoa.setEnabled(true);
                     binding.btnXoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red));
                     loadData();
                  });
               }
            });
         }
      });

      // Xử lý nút "Xóa tất cả"
      binding.btnXoa.setOnClickListener(v -> {
         new AlertDialog.Builder(requireContext())
                 .setTitle("Xác nhận xóa")
                 .setMessage("Bạn có chắc chắn muốn xóa tất cả không?")
                 .setPositiveButton("Xóa", (dialog, which) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                       keyDAO.deleteAll();
                       loadData();
                    });
                 })
                 .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                 .show();
      });
      // Sửa
      binding.btnSua.setOnClickListener(v -> {
         if (selectedKey == null) {
            Toast.makeText(requireContext(), "Vui lòng chọn một từ khóa để sửa!", Toast.LENGTH_SHORT).show();
            return;
         }

         if (binding.btnSua.getText().toString().equals("Sửa")) {
            // Bật EditText cho phép chỉnh sửa
            binding.btnHuy.setVisibility(View.VISIBLE);
            binding.editTuKhoa.setEnabled(true);
            binding.editTuKhoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
            binding.editTuKhoa.requestFocus();
            binding.btnThem.setEnabled(false);
            binding.btnThem.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.silver));
            binding.btnXoa.setEnabled(false);
            binding.btnXoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.silver));
            binding.btnSua.setText("Lưu");
         }
         else
         {
            String newWord = binding.editTuKhoa.getText().toString().trim();
            if (newWord.isEmpty()) {
               Toast.makeText(requireContext(), "Vui lòng nhập từ khóa!", Toast.LENGTH_SHORT).show();
               return;
            }

            // Cập nhật từ khóa trong database
            Executors.newSingleThreadExecutor().execute(() -> {
               selectedKey.setKeyword(newWord);
               keyDAO.update(selectedKey);
               requireActivity().runOnUiThread(() -> {
                  binding.editTuKhoa.setText("");
                  binding.editTuKhoa.setEnabled(false);
                  binding.editTuKhoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.silver));
                  binding.btnSua.setText("Sửa");
                  binding.btnThem.setEnabled(true);
                  binding.btnThem.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.mauthem));
                  binding.btnXoa.setEnabled(true);
                  binding.btnXoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red));
                  selectedKey = null;
                  loadData();
               });
            });
         }
      });
      executor = Executors.newSingleThreadExecutor();
      unlockPrefs = requireContext().getSharedPreferences("UnlockPrefs", MODE_PRIVATE);

   }

   @SuppressLint("SetTextI18n")
   private void clickHuy() {
      binding.editTuKhoa.setText("");
      binding.editTuKhoa.setEnabled(false);
      binding.editTuKhoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.silver));
      binding.btnThem.setText("Thêm");
      binding.btnThem.setEnabled(true);
      binding.btnThem.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.mauthem));
      binding.btnXoa.setEnabled(true);
      binding.btnXoa.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.red));
      binding.btnSua.setText("Sửa");
      binding.btnSua.setEnabled(true);
      binding.btnSua.setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.teal));
      binding.btnHuy.setVisibility(View.GONE);
   }

   private void loadData() {
      viewModel.getAllKeywords().observe(getViewLifecycleOwner(), list -> {
         adapter.submitList(null); // Reset danh sách trước để tránh lỗi UI
         adapter.submitList(list); // Cập nhật danh sách mới
      });
   }

   private void showBlockScreen(String keyword) {
      Intent intent = new Intent(requireContext(), BlockGKTHActivity.class);
      intent.putExtra("blocked_keyword", keyword);
      startActivity(intent);
   }

   @Override
   public void onDestroy() {
      super.onDestroy();
      executor.shutdown();
   }
//   public void checktu(String text) {
//      Executors.newSingleThreadExecutor().execute(() -> {
//         List<Key> keywords = keyDAO.getAllKeywords();
//         boolean isBlocked = false;
//
//         for (Key keyword : keywords) {
//            if (text.toLowerCase().contains(keyword.getKeyword().toLowerCase())) {
//               isBlocked = true;
//               break;
//            }
//         }
//
//         if (isBlocked && !isUnlocked()) {
//            requireActivity().runOnUiThread(() -> {
//               Intent intent = new Intent(requireContext(), BlockGKTHActivity.class);
//               startActivity(intent);
//            });
//         }
//      });
//   }
public void checkSearchQuery(String query) {
   if (unlockPrefs.getBoolean("is_unlocked", false)) {
      Log.d(TAG, "Bỏ qua kiểm tra do đã unlock");
      return;
   }

   executor.execute(() -> {
      List<Key> blockedKeywords = keyDAO.getAllKeywords();
      boolean isBlocked = false;
      String matchedKeyword = null;

      for (Key keyword : blockedKeywords) {
         if (query.toLowerCase().contains(keyword.getKeyword().toLowerCase())) {
            isBlocked = true;
            matchedKeyword = keyword.getKeyword();
            break;
         }
      }

      if (isBlocked && matchedKeyword != null) {
         String finalMatchedKeyword = matchedKeyword;
         requireActivity().runOnUiThread(() -> {
            showBlockScreen(finalMatchedKeyword);
         });
      }
   });
}
   private boolean isUnlocked() {
      return requireContext()
              .getSharedPreferences("UnlockPrefs", MODE_PRIVATE)
              .getBoolean("is_unlocked", false);
   }
   private final BroadcastReceiver searchReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
         if ("ACTION_CHECK_SEARCH".equals(intent.getAction())) {
            String query = intent.getStringExtra("query");
            if (query != null) {
               checkSearchQuery(query);
            }
         }
      }
   };
   @Override
   public void onResume() {
      super.onResume();
      requireContext().registerReceiver(
              searchReceiver,
              new IntentFilter("ACTION_CHECK_SEARCH")
      );
   }

   @Override
   public void onPause() {
      super.onPause();
      requireContext().unregisterReceiver(searchReceiver);
   }
}
