package com.example.recipeapp;

import com.example.recipeapp.draw.DrawActivity;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private LinearLayout mainLayout;

    private ActivityResultLauncher<Intent> drawActivityLauncher;
    private int recognizedNumber = -1; // DrawActivity에서 전달받은 숫자
    private List<Ingredient> ingredientList = new ArrayList<>(); // 식재료 목록
    private Map<String, LinearLayout> categoryLayoutMap = new HashMap<>(); // 카테고리별 레이아웃 관리

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 카테고리 버튼
        Button categoryButton = findViewById(R.id.btnCategory);
        categoryButton.setOnClickListener(view -> {
            showCategoryDialog(categoryButton);
        });

        // 입력 필드와 확인 버튼 동작
        EditText inputField = findViewById(R.id.inputField);
        Button confirmButton = findViewById(R.id.btnConfirm);
        confirmButton.setOnClickListener(view -> {
            String inputText = inputField.getText().toString().trim();  // 입력값 가져오기 (공백 제거)

            if (inputText.isEmpty()) {
                Toast.makeText(this, "입력을 확인해주세요!", Toast.LENGTH_SHORT).show();
            } else {
                // 키보드 자동으로 내리기
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(inputField.getWindowToken(), 0);  // 키보드 내리기
            }
        });

        // ActivityResultLauncher 초기화
        drawActivityLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        recognizedNumber = result.getData().getIntExtra("recognizedNumber", -1);
                        Toast.makeText(this, "판독된 수량: " + recognizedNumber, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // 캔버스 열기
        Button drawButton = findViewById(R.id.btnDraw);
        drawButton.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, DrawActivity.class);
            drawActivityLauncher.launch(intent); // 새로운 방식으로 액티비티 시작
        });

        //항목 카테고리에 추가
        mainLayout = findViewById(R.id.ingredientLayout);
        Button addButton = findViewById(R.id.btnAdd);

        addButton.setOnClickListener(view -> {
            String category = categoryButton.getText().toString();
            String ingredientName = inputField.getText().toString().trim();
            int num = recognizedNumber;

            if (category.equals("Category") || ingredientName.isEmpty() || num == -1) {
                Toast.makeText(this, "카테고리와 식재료, 수량을 확인해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Ingredient 객체 생성 및 추가
            Ingredient ingredient = new Ingredient(category, ingredientName, num);
            ingredientList.add(ingredient);

            // 카테고리별 UI 갱신
            addIngredientToCategoryLayout(ingredient);
            Toast.makeText(this, "식재료가 추가되었습니다.", Toast.LENGTH_SHORT).show();

            // 초기화
            categoryButton.setText("Category");
            inputField.setText("");
            recognizedNumber = -1;
        });
    }

    private void createCategoryLayout(String category) {
        // 카테고리 레이아웃 생성
        LinearLayout categoryLayout = new LinearLayout(this);
        categoryLayout.setOrientation(LinearLayout.VERTICAL);
        categoryLayout.setPadding(20, 20, 20, 20);

        // 카테고리 제목 추가
        TextView categoryTitle = new TextView(this);
        categoryTitle.setText(category);
        categoryTitle.setTextSize(18);
        categoryTitle.setPadding(10, 10, 10, 10);
        categoryTitle.setGravity(Gravity.CENTER);

        // 점선 추가
        View dashedLine = new View(this);
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 4); // 가로 길이 채우기, 높이 4dp로 설정
        lineParams.setMargins(0, 10, 0, 10); // 위아래 여백 설정
        dashedLine.setLayoutParams(lineParams);
        dashedLine.setBackgroundResource(R.drawable.dashed_line); // 점선 배경 적용

        // 메인 레이아웃에 추가
        mainLayout.addView(categoryTitle);
        mainLayout.addView(dashedLine); // 점선 추가
        mainLayout.addView(categoryLayout);

        // Map에 저장
        categoryLayoutMap.put(category, categoryLayout);
    }

    private void showCategoryDialog(Button categoryButton) {
        // 기존 카테고리 목록을 동적으로 갱신
        final List<String> categoriesList = new ArrayList<>(categoryLayoutMap.keySet());

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("카테고리 선택 또는 추가");

        // 갱신된 카테고리 목록 표시
        builder.setItems(categoriesList.toArray(new String[0]), (dialog, which) -> {
            String selectedCategory = categoriesList.get(which);
            categoryButton.setText(selectedCategory);
        });

        // 카테고리 추가 버튼
        builder.setPositiveButton("카테고리 추가", (dialog, which) -> {
            final EditText input = new EditText(this);
            input.setHint("새 카테고리를 입력하세요");

            new AlertDialog.Builder(this)
                    .setTitle("새 카테고리 추가")
                    .setView(input)
                    .setPositiveButton("추가", (dialog1, which1) -> {
                        String newCategory = input.getText().toString().trim();
                        if (!newCategory.isEmpty()) {
                            if (!categoryLayoutMap.containsKey(newCategory)) {
                                createCategoryLayout(newCategory); // 새 카테고리 레이아웃 생성
                                Toast.makeText(this, newCategory + " 카테고리가 추가되었습니다.", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(this, "이미 존재하는 카테고리입니다.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "카테고리 이름을 입력해주세요.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        // 카테고리 삭제 버튼
        builder.setNeutralButton("카테고리 삭제", (dialog, which) -> {
            if (categoryLayoutMap.isEmpty()) {
                Toast.makeText(this, "삭제할 카테고리가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            final List<String> currentCategories = new ArrayList<>(categoryLayoutMap.keySet());
            new AlertDialog.Builder(this)
                    .setTitle("삭제할 카테고리를 선택하세요")
                    .setItems(currentCategories.toArray(new String[0]), (deleteDialog, selectedIndex) -> {
                        String selectedCategory = currentCategories.get(selectedIndex);
                        deleteCategory(selectedCategory);
                        Toast.makeText(this, selectedCategory + " 카테고리가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        categoryButton.setText("Category");
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        builder.setCancelable(true);
        builder.show();
    }

    private void addIngredientToCategoryLayout(Ingredient ingredient) {
        String category = ingredient.getCategory();

        // 카테고리 레이아웃이 없으면 새로 생성
        if (!categoryLayoutMap.containsKey(category)) {
            createCategoryLayout(category);
        }

        // 해당 카테고리 레이아웃에 재료 추가
        LinearLayout categoryLayout = categoryLayoutMap.get(category);

        // 개별 재료 레이아웃 생성 (수평 정렬)
        LinearLayout ingredientLayout = new LinearLayout(this);
        ingredientLayout.setOrientation(LinearLayout.HORIZONTAL);
        ingredientLayout.setPadding(10, 10, 10, 10);
        ingredientLayout.setBackgroundResource(R.drawable.border_box); // 테두리 추가

        // 항목 이름 (좌측 정렬)
        TextView ingredientNameView = new TextView(this);
        ingredientNameView.setText(ingredient.getName());
        ingredientNameView.setTextSize(16);
        ingredientNameView.setPadding(10, 15, 10, 15); // 상단과 하단 여백을 추가
        ingredientNameView.setGravity(Gravity.CENTER_VERTICAL); // 텍스트를 세로 중심 정렬

        // weight를 사용해 좌측과 우측을 분리
        LinearLayout.LayoutParams ingredientNameLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        ingredientNameView.setLayoutParams(ingredientNameLayoutParams);

        // 수량 표시 TextView (우측 정렬)
        TextView quantityView = new TextView(this);
        quantityView.setText(String.valueOf(ingredient.getNum())); // 초기 수량 설정
        quantityView.setTextSize(20);
        quantityView.setPadding(10, 10, 10, 10);

        // 버튼 레이아웃 (우측 정렬)
        LinearLayout buttonsLayout = new LinearLayout(this);
        buttonsLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(100, LinearLayout.LayoutParams.WRAP_CONTENT);

        // - 버튼
        Button minusButton = new Button(this);
        minusButton.setText("-");
        minusButton.setTextSize(18); // 텍스트 크기 조정
        minusButton.setPadding(5, 5, 5, 5); // 패딩 조정
        minusButton.setLayoutParams(buttonLayoutParams);
        minusButton.setOnClickListener(view -> {
            // 수량 감소 (최소 0으로 제한)
            if (ingredient.getNum() > 0) {
                ingredient.setNum(ingredient.getNum() - 1);
                quantityView.setText(String.valueOf(ingredient.getNum())); // 수량 갱신
            }
        });

        // + 버튼
        Button plusButton = new Button(this);
        plusButton.setText("+");
        plusButton.setTextSize(18); // 텍스트 크기 조정
        plusButton.setPadding(5, 5, 5, 5); // 패딩 조정
        plusButton.setLayoutParams(buttonLayoutParams);
        plusButton.setOnClickListener(view -> {
            // 수량 증가
            ingredient.setNum(ingredient.getNum() + 1);
            quantityView.setText(String.valueOf(ingredient.getNum())); // 수량 갱신
        });

        // 삭제 버튼
        Button deleteButton = new Button(this);
        deleteButton.setText("X");
        deleteButton.setTextSize(18); // 텍스트 크기 조정
        deleteButton.setPadding(5, 5, 5, 5); // 패딩 조정
        deleteButton.setLayoutParams(buttonLayoutParams);
        deleteButton.setOnClickListener(view -> {
            // 해당 재료 삭제
            ingredientList.remove(ingredient); // 데이터에서 삭제
            categoryLayout.removeView(ingredientLayout); // UI에서 삭제
        });

        // 버튼 레이아웃에 버튼 추가
        buttonsLayout.addView(minusButton);
        buttonsLayout.addView(quantityView);
        buttonsLayout.addView(plusButton);
        buttonsLayout.addView(deleteButton);

        // ingredientLayout에 항목 이름과 버튼 레이아웃 추가
        ingredientLayout.addView(ingredientNameView); // 좌측: 항목 이름
        ingredientLayout.addView(buttonsLayout); // 우측: 버튼들

        // 카테고리 레이아웃에 추가
        categoryLayout.addView(ingredientLayout);
    }

    private void deleteCategory(String category) {
        // 관련된 데이터 삭제
        ingredientList.removeIf(ingredient -> ingredient.getCategory().equals(category));

        // 카테고리 제목과 점선 삭제
        for (int i = 0; i < mainLayout.getChildCount(); i++) {
            View view = mainLayout.getChildAt(i);

            // 제목 확인 및 삭제
            if (view instanceof TextView) {
                TextView textView = (TextView) view;
                if (textView.getText().toString().equals(category)) {
                    // 제목 삭제
                    mainLayout.removeViewAt(i);

                    // 제목 아래 점선 삭제
                    if (i < mainLayout.getChildCount()) {
                        View nextView = mainLayout.getChildAt(i);
                        if (nextView.getBackground() != null) { // 점선인지 확인
                            mainLayout.removeViewAt(i); // 점선 삭제
                        }
                    }
                    break;
                }
            }
        }

        // 카테고리 레이아웃 삭제
        LinearLayout categoryLayout = categoryLayoutMap.get(category);
        if (categoryLayout != null) {
            mainLayout.removeView(categoryLayout); // 메인 레이아웃에서 삭제
            categoryLayoutMap.remove(category); // 맵에서도 제거
        }
    }
}