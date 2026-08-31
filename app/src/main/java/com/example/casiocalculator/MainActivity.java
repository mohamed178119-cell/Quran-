package com.example.casiocalculator;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Button;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class MainActivity extends Activity {
    private TextView expressionView;
    private TextView displayView;
    private BigDecimal accumulator = BigDecimal.ZERO;
    private String pendingOperator = "";
    private String currentInput = "0";
    private boolean startNewNumber = true;
    private boolean degreeMode = true;
    private boolean inverseMode = false;
    private static final MathContext MC = new MathContext(16, RoundingMode.HALF_UP);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildCalculator());
        refreshDisplay();
    }

    private View buildCalculator() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(18, 18, 18, 18);
        root.setBackgroundColor(Color.rgb(36, 42, 46));

        TextView brand = new TextView(this);
        brand.setText("CASIO  fx-991 CLASSIC");
        brand.setTextColor(Color.WHITE);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setTextSize(20);
        brand.setGravity(Gravity.CENTER);
        root.addView(brand, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout screen = new LinearLayout(this);
        screen.setOrientation(LinearLayout.VERTICAL);
        screen.setPadding(16, 12, 16, 12);
        screen.setBackgroundColor(Color.rgb(183, 196, 166));
        expressionView = new TextView(this);
        expressionView.setTextColor(Color.rgb(45, 58, 45));
        expressionView.setTextSize(16);
        expressionView.setGravity(Gravity.END);
        displayView = new TextView(this);
        displayView.setTextColor(Color.rgb(11, 24, 11));
        displayView.setTextSize(34);
        displayView.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        displayView.setGravity(Gravity.END);
        screen.addView(expressionView, new LinearLayout.LayoutParams(-1, -2));
        screen.addView(displayView, new LinearLayout.LayoutParams(-1, -2));
        LinearLayout.LayoutParams screenParams = new LinearLayout.LayoutParams(-1, -2);
        screenParams.setMargins(0, 16, 0, 18);
        root.addView(screen, screenParams);

        GridLayout keys = new GridLayout(this);
        keys.setColumnCount(5);
        String[] labels = {
            "SHIFT", "DRG", "sin", "cos", "tan",
            "√", "x²", "xʸ", "log", "ln",
            "7", "8", "9", "DEL", "AC",
            "4", "5", "6", "×", "÷",
            "1", "2", "3", "+", "−",
            "0", ".", "±", "EXP", "=",
            "π", "e", "1/x", "n!", "%"
        };
        for (String label : labels) {
            keys.addView(makeButton(label));
        }
        root.addView(keys, new LinearLayout.LayoutParams(-1, 0, 1));
        return root;
    }

    private Button makeButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setTextColor(Color.WHITE);
        button.setBackgroundColor(colorFor(label));
        button.setOnClickListener(view -> press(label));
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = 0;
        params.height = GridLayout.LayoutParams.WRAP_CONTENT;
        params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
        params.setMargins(5, 5, 5, 5);
        button.setLayoutParams(params);
        return button;
    }

    private int colorFor(String label) {
        if ("AC".equals(label) || "DEL".equals(label)) return Color.rgb(156, 48, 54);
        if ("SHIFT".equals(label) || "DRG".equals(label)) return Color.rgb(181, 126, 31);
        if ("+−×÷=".contains(label)) return Color.rgb(42, 75, 118);
        if (label.matches("[0-9.]") || "EXP".equals(label)) return Color.rgb(58, 62, 68);
        return Color.rgb(74, 80, 88);
    }

    private void press(String key) {
        if (key.matches("[0-9]")) appendDigit(key);
        else if (".".equals(key)) appendDecimal();
        else if ("AC".equals(key)) clearAll();
        else if ("DEL".equals(key)) deleteLast();
        else if ("+−×÷xʸ".contains(key)) queueOperator(key);
        else if ("=".equals(key)) evaluate();
        else if ("±".equals(key)) toggleSign();
        else if ("EXP".equals(key)) currentInput = format(value().multiply(BigDecimal.TEN.pow(3), MC));
        else if ("SHIFT".equals(key)) inverseMode = !inverseMode;
        else if ("DRG".equals(key)) degreeMode = !degreeMode;
        else applyFunction(key);
        refreshDisplay();
    }

    private void appendDigit(String digit) {
        if ("Error".equals(currentInput) || startNewNumber || "0".equals(currentInput)) currentInput = digit;
        else currentInput += digit;
        startNewNumber = false;
    }

    private void appendDecimal() {
        if ("Error".equals(currentInput) || startNewNumber) currentInput = "0";
        if (!currentInput.contains(".")) currentInput += ".";
        startNewNumber = false;
    }

    private void queueOperator(String operator) {
        if (!pendingOperator.isEmpty() && !startNewNumber) computePending();
        else accumulator = value();
        pendingOperator = operator;
        startNewNumber = true;
    }

    private void evaluate() {
        computePending();
        pendingOperator = "";
        startNewNumber = true;
    }

    private void computePending() {
        BigDecimal right = value();
        if (pendingOperator.isEmpty()) accumulator = right;
        else if ("+".equals(pendingOperator)) accumulator = accumulator.add(right, MC);
        else if ("−".equals(pendingOperator)) accumulator = accumulator.subtract(right, MC);
        else if ("×".equals(pendingOperator)) accumulator = accumulator.multiply(right, MC);
        else if ("÷".equals(pendingOperator)) accumulator = right.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : accumulator.divide(right, MC);
        else if ("xʸ".equals(pendingOperator)) accumulator = BigDecimal.valueOf(Math.pow(accumulator.doubleValue(), right.doubleValue()));
        currentInput = format(accumulator);
    }

    private void applyFunction(String key) {
        double number = value().doubleValue();
        double result = number;
        if ("sin".equals(key)) result = inverseMode ? fromRadians(Math.asin(number)) : Math.sin(angle(number));
        else if ("cos".equals(key)) result = inverseMode ? fromRadians(Math.acos(number)) : Math.cos(angle(number));
        else if ("tan".equals(key)) result = inverseMode ? fromRadians(Math.atan(number)) : Math.tan(angle(number));
        else if ("√".equals(key)) result = Math.sqrt(number);
        else if ("x²".equals(key)) result = number * number;
        else if ("log".equals(key)) result = Math.log10(number);
        else if ("ln".equals(key)) result = Math.log(number);
        else if ("π".equals(key)) result = Math.PI;
        else if ("e".equals(key)) result = Math.E;
        else if ("1/x".equals(key)) result = 1.0 / number;
        else if ("n!".equals(key)) result = factorial(number);
        else if ("%".equals(key)) result = number / 100.0;
        currentInput = Double.isFinite(result) ? format(BigDecimal.valueOf(result)) : "Error";
        startNewNumber = true;
        inverseMode = false;
    }

    private double angle(double number) {
        return degreeMode ? Math.toRadians(number) : number;
    }

    private double fromRadians(double radians) {
        return degreeMode ? Math.toDegrees(radians) : radians;
    }

    private double factorial(double number) {
        if (number < 0 || number > 170 || number != Math.floor(number)) return Double.NaN;
        double result = 1.0;
        for (int index = 2; index <= (int) number; index++) result *= index;
        return result;
    }

    private void toggleSign() {
        if (currentInput.startsWith("-")) currentInput = currentInput.substring(1);
        else if (!"0".equals(currentInput)) currentInput = "-" + currentInput;
    }

    private void deleteLast() {
        if (currentInput.length() <= 1 || startNewNumber) currentInput = "0";
        else currentInput = currentInput.substring(0, currentInput.length() - 1);
    }

    private void clearAll() {
        accumulator = BigDecimal.ZERO;
        pendingOperator = "";
        currentInput = "0";
        startNewNumber = true;
        inverseMode = false;
    }

    private BigDecimal value() {
        if ("Error".equals(currentInput)) return BigDecimal.ZERO;
        return new BigDecimal(currentInput, MC);
    }

    private String format(BigDecimal number) {
        return number.round(MC).stripTrailingZeros().toPlainString();
    }

    private void refreshDisplay() {
        String mode = degreeMode ? "DEG" : "RAD";
        String shift = inverseMode ? " SHIFT" : "";
        expressionView.setText(mode + shift + "  " + accumulator.toPlainString() + " " + pendingOperator);
        displayView.setText(currentInput.length() > 16 ? currentInput.substring(0, 16) : currentInput);
    }
}
