package com.example.data.local

enum class AlgoTopicCategory(val labelBn: String) {
    ALL("সবগুলো"),
    MATH_FORMULA("গাণিতিক ম্যাথ"),
    DECISION("শর্তমূলক (If-Else)"),
    LOOP_SERIES("লুপ ও ধারা")
}

enum class FlowNodeShape {
    TERMINAL,      // Oval: Start / Stop
    INPUT_OUTPUT,  // Parallelogram: Input / Print
    PROCESS,       // Rectangle: Calculation / Assignment
    DECISION       // Diamond: Condition (Yes / No)
}

sealed class FlowchartDiagramSpec {
    data class Linear(
        val steps: List<Pair<FlowNodeShape, String>>
    ) : FlowchartDiagramSpec()

    data class TwoBranchDecision(
        val startLabel: String,
        val inputLabel: String,
        val preProcessLabel: String? = null,
        val conditionLabel: String,
        val yesBranchLabel: String,
        val noBranchLabel: String,
        val endLabel: String = "Stop / শেষ"
    ) : FlowchartDiagramSpec()

    data class LoopFlow(
        val startLabel: String,
        val inputLabel: String,
        val initLabel: String,
        val conditionLabel: String,
        val bodyProcessLabel: String,
        val updateLabel: String,
        val outputLabel: String,
        val endLabel: String = "Stop / শেষ"
    ) : FlowchartDiagramSpec()
}

data class FlowchartSymbolInfo(
    val id: String,
    val nameBn: String,
    val nameEn: String,
    val shape: FlowNodeShape,
    val purposeBn: String,
    val exampleText: String
)

data class AlgorithmFlowchartTopic(
    val id: String,
    val category: AlgoTopicCategory,
    val titleBn: String,
    val subtitleEn: String,
    val formulaTitleBn: String,
    val formulaText: String,
    val mathExampleTitleBn: String,
    val mathStepsBn: List<String>,
    val algorithmStepsBn: List<String>,
    val flowchart: FlowchartDiagramSpec,
    val cCode: String,
    val sampleStdin: String = ""
)

object AlgorithmFlowchartData {

    val symbols: List<FlowchartSymbolInfo> = listOf(
        FlowchartSymbolInfo(
            id = "sym_terminal",
            nameBn = "শুরু ও শেষ (টার্মিনাল)",
            nameEn = "Oval / Terminal",
            shape = FlowNodeShape.TERMINAL,
            purposeBn = "ফ্লোচার্টের প্রোগ্রাম শুরু (Start) এবং শেষ (Stop/End) বোঝাতে উপবৃত্ত বা ডিম্বাকৃতি প্রতীক ব্যবহৃত হয়।",
            exampleText = "Start / Stop"
        ),
        FlowchartSymbolInfo(
            id = "sym_io",
            nameBn = "ইনপুট ও আউটপুট",
            nameEn = "Parallelogram (Input/Output)",
            shape = FlowNodeShape.INPUT_OUTPUT,
            purposeBn = "ব্যবহারকারীর কাছ থেকে মান গ্রহণ (Input/Read) এবং ফলাফল প্রদর্শন (Print/Display) করতে সামান্তরিক প্রতীক বসে।",
            exampleText = "Input a, b / Print Sum"
        ),
        FlowchartSymbolInfo(
            id = "sym_process",
            nameBn = "প্রক্রিয়াকরণ (প্রসেস)",
            nameEn = "Rectangle (Process)",
            shape = FlowNodeShape.PROCESS,
            purposeBn = "সব ধরনের গাণিতিক হিসাব, সূত্র প্রয়োগ এবং ভেরিয়েবলের মান পরিবর্তনের জন্য আয়তক্ষেত্র ব্যবহৃত হয়।",
            exampleText = "Area = 0.5 * b * h"
        ),
        FlowchartSymbolInfo(
            id = "sym_decision",
            nameBn = "সিদ্ধান্ত গ্রহণ (শর্ত)",
            nameEn = "Diamond (Decision)",
            shape = FlowNodeShape.DECISION,
            purposeBn = "যেকোনো শর্ত যাচাই (হ্যাঁ/না বা True/False) করতে রম্বস বা হীরক আকৃতির প্রতীক ব্যবহৃত হয়। এখান থেকে ২টি প্রবাহ বের হয়।",
            exampleText = "Is a > b ?"
        )
    )

    val topics: List<AlgorithmFlowchartTopic> = listOf(
        // 1. Sum & Average of 3 numbers
        AlgorithmFlowchartTopic(
            id = "algo_sum_avg",
            category = AlgoTopicCategory.MATH_FORMULA,
            titleBn = "৩টি সংখ্যার যোগফল ও গড় নির্ণয়",
            subtitleEn = "Sum and Average of Three Numbers",
            formulaTitleBn = "গাণিতিক সূত্র",
            formulaText = "Sum = a + b + c\nAverage = Sum ÷ 3",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি a = 10, b = 20, c = 30)",
            mathStepsBn = listOf(
                "ইনপুট মান: a = 10, b = 20, c = 30",
                "যোগফল, Sum = 10 + 20 + 30 = 60",
                "গড়, Avg = 60 ÷ 3 = 20.00",
                "ফলাফল: Sum = 60 এবং Avg = 20.00"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: প্রোগ্রাম শুরু করি (Start)।",
                "ধাপ ২: তিনটি সংখ্যা a, b ও c এর মান ইনপুট হিসেবে গ্রহণ করি।",
                "ধাপ ৩: Sum = a + b + c সূত্রের সাহায্যে যোগফল নির্ণয় করি।",
                "ধাপ ৪: Avg = Sum / 3.0 সূত্রের সাহায্যে গড় নির্ণয় করি।",
                "ধাপ ৫: Sum এবং Avg এর মান ফলাফল হিসেবে প্রিন্ট করি।",
                "ধাপ ৬: প্রোগ্রাম শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.Linear(
                steps = listOf(
                    FlowNodeShape.TERMINAL to "Start (শুরু)",
                    FlowNodeShape.INPUT_OUTPUT to "Input a, b, c",
                    FlowNodeShape.PROCESS to "Sum = a + b + c\nAvg = Sum / 3.0",
                    FlowNodeShape.INPUT_OUTPUT to "Print Sum, Avg",
                    FlowNodeShape.TERMINAL to "Stop (শেষ)"
                )
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    int a, b, c, sum;
                    float avg;
                    scanf("%d %d %d", &a, &b, &c);
                    sum = a + b + c;
                    avg = sum / 3.0;
                    printf("Sum = %d\nAvg = %.2f\n", sum, avg);
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "10 20 30"
        ),

        // 2. Triangle Area (Heron's Formula - 3 sides)
        AlgorithmFlowchartTopic(
            id = "algo_triangle_heron",
            category = AlgoTopicCategory.MATH_FORMULA,
            titleBn = "বিষমবাহু ত্রিভুজের ক্ষেত্রফল (৩ বাহু)",
            subtitleEn = "Area of Triangle (Heron's Formula)",
            formulaTitleBn = "গাণিতিক সূত্র",
            formulaText = "s = (a + b + c) ÷ 2\nArea = √(s × (s - a) × (s - b) × (s - c))",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি a = 3, b = 4, c = 5)",
            mathStepsBn = listOf(
                "অর্ধ-পরিসীমা, s = (3 + 4 + 5) ÷ 2 = 12 ÷ 2 = 6",
                "সূত্রে বসিয়ে: s(s-a)(s-b)(s-c) = 6 × (6-3) × (6-4) × (6-5)",
                "গুণফল = 6 × 3 × 2 × 1 = 36",
                "ক্ষেত্রফল, Area = √36 = 6.00 বর্গ একক"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: ত্রিভুজের তিন বাহু a, b, c এর মান ইনপুট নিই।",
                "ধাপ ৩: শর্ত যাচাই করি: (a+b>c) এবং (b+c>a) এবং (c+a>b) সত্য কি না।",
                "ধাপ ৪: সত্য হলে s = (a + b + c) / 2.0 এবং Area = √(s(s-a)(s-b)(s-c)) হিসাব করি।",
                "ধাপ ৫: মিথ্যা হলে \"Triangle not possible\" দেখাই, সত্য হলে Area প্রিন্ট করি।",
                "ধাপ ৬: প্রোগ্রাম শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.TwoBranchDecision(
                startLabel = "Start (শুরু)",
                inputLabel = "Input a, b, c",
                conditionLabel = "a+b>c && b+c>a\n&& c+a>b ?",
                yesBranchLabel = "s = (a+b+c)/2\nArea = √(s(s-a)(s-b)(s-c))\nPrint Area",
                noBranchLabel = "Print \"Invalid\nTriangle\"",
                endLabel = "Stop (শেষ)"
            ),
            cCode = """
                #include <stdio.h>
                #include <math.h>
                int main() {
                    float a, b, c, s, area;
                    scanf("%f %f %f", &a, &b, &c);
                    if (a + b > c && b + c > a && c + a > b) {
                        s = (a + b + c) / 2.0;
                        area = sqrt(s * (s - a) * (s - b) * (s - c));
                        printf("Area = %.2f\n", area);
                    } else {
                        printf("Triangle not possible\n");
                    }
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "3 4 5"
        ),

        // 3. Celsius to Fahrenheit
        AlgorithmFlowchartTopic(
            id = "algo_temp_c_to_f",
            category = AlgoTopicCategory.MATH_FORMULA,
            titleBn = "সেলসিয়াস থেকে ফারেনহাইট রূপান্তর",
            subtitleEn = "Celsius to Fahrenheit Conversion",
            formulaTitleBn = "গাণিতিক সূত্র",
            formulaText = "F = (9 × C) ÷ 5 + 32   |   C = 5 × (F - 32) ÷ 9",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি C = 37° সেলসিয়াস)",
            mathStepsBn = listOf(
                "দেওয়া আছে, C = 37°C",
                "F = (9 × 37) ÷ 5 + 32",
                "F = 333 ÷ 5 + 32 = 66.6 + 32",
                "F = 98.60° ফারেনহাইট"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: সেলসিয়াস তাপমাত্রা C এর মান ইনপুট নিই।",
                "ধাপ ৩: F = (9.0 * C) / 5.0 + 32.0 সূত্র ব্যবহার করে হিসাব করি।",
                "ধাপ ৪: ফারেনহাইট তাপমাত্রা F প্রিন্ট করি।",
                "ধাপ ৫: শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.Linear(
                steps = listOf(
                    FlowNodeShape.TERMINAL to "Start (শুরু)",
                    FlowNodeShape.INPUT_OUTPUT to "Input Celsius C",
                    FlowNodeShape.PROCESS to "F = (9 * C) / 5 + 32",
                    FlowNodeShape.INPUT_OUTPUT to "Print F",
                    FlowNodeShape.TERMINAL to "Stop (শেষ)"
                )
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    float c, f;
                    scanf("%f", &c);
                    f = (9.0 * c) / 5.0 + 32.0;
                    printf("Fahrenheit = %.2f\n", f);
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "37"
        ),

        // 4. Circle Area & Circumference
        AlgorithmFlowchartTopic(
            id = "algo_circle",
            category = AlgoTopicCategory.MATH_FORMULA,
            titleBn = "বৃত্তের ক্ষেত্রফল ও পরিধি নির্ণয়",
            subtitleEn = "Area & Circumference of a Circle",
            formulaTitleBn = "গাণিতিক সূত্র (π = 3.1416)",
            formulaText = "Area = π × r²\nCircumference = 2 × π × r",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি ব্যাসার্ধ r = 5 একক)",
            mathStepsBn = listOf(
                "ব্যাসার্ধ r = 5, π = 3.1416",
                "ক্ষেত্রফল, Area = 3.1416 × 5 × 5 = 3.1416 × 25 = 78.54",
                "পরিধি, C = 2 × 3.1416 × 5 = 31.416 ≈ 31.42",
                "ফলাফল: Area = 78.54, Circumference = 31.42"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: বৃত্তের ব্যাসার্ধ r এর মান ইনপুট নিই।",
                "ধাপ ৩: Area = 3.1416 * r * r হিসাব করি।",
                "ধাপ ৪: Circ = 2 * 3.1416 * r হিসাব করি।",
                "ধাপ ৫: Area ও Circ প্রিন্ট করি।",
                "ধাপ ৬: প্রোগ্রাম শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.Linear(
                steps = listOf(
                    FlowNodeShape.TERMINAL to "Start (শুরু)",
                    FlowNodeShape.INPUT_OUTPUT to "Input radius r",
                    FlowNodeShape.PROCESS to "Area = 3.1416 * r * r\nCirc = 2 * 3.1416 * r",
                    FlowNodeShape.INPUT_OUTPUT to "Print Area, Circ",
                    FlowNodeShape.TERMINAL to "Stop (শেষ)"
                )
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    float r, area, circ;
                    scanf("%f", &r);
                    area = 3.1416 * r * r;
                    circ = 2 * 3.1416 * r;
                    printf("Area = %.2f\nCirc = %.2f\n", area, circ);
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "5"
        ),

        // 5. Even or Odd Number
        AlgorithmFlowchartTopic(
            id = "algo_even_odd",
            category = AlgoTopicCategory.DECISION,
            titleBn = "জোড় ও বিজোড় সংখ্যা নির্ণয়",
            subtitleEn = "Even or Odd Number Check",
            formulaTitleBn = "ভাগশেষের শর্ত (Modulus)",
            formulaText = "যদি (n % 2 == 0) হয় → জোড় (Even)\nঅন্যথায় (ভাগশেষ ১ হলে) → বিজোড় (Odd)",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি n = 17 এবং n = 24)",
            mathStepsBn = listOf(
                "n = 17 হলে: 17 ÷ 2 করলে ভাগফল 8 এবং ভাগশেষ (17 % 2) = 1 ≠ 0 → বিজোড় (Odd)",
                "n = 24 হলে: 24 ÷ 2 করলে ভাগফল 12 এবং ভাগশেষ (24 % 2) = 0 → জোড় (Even)"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: একটি পূর্ণসংখ্যা n ইনপুট নিই।",
                "ধাপ ৩: যাচাই করি n % 2 == 0 কি না।",
                "ধাপ ৪: যদি শর্ত সত্য হয়, তবে \"Even\" প্রিন্ট করি।",
                "ধাপ ৫: অন্যথায় \"Odd\" প্রিন্ট করি।",
                "ধাপ ৬: শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.TwoBranchDecision(
                startLabel = "Start (শুরু)",
                inputLabel = "Input integer n",
                conditionLabel = "n % 2 == 0 ?",
                yesBranchLabel = "Print \"Even\"",
                noBranchLabel = "Print \"Odd\"",
                endLabel = "Stop (শেষ)"
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    int n;
                    scanf("%d", &n);
                    if (n % 2 == 0) {
                        printf("%d is Even\n", n);
                    } else {
                        printf("%d is Odd\n", n);
                    }
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "17"
        ),

        // 6. Largest of 3 Numbers
        AlgorithmFlowchartTopic(
            id = "algo_largest_3",
            category = AlgoTopicCategory.DECISION,
            titleBn = "৩টি সংখ্যার মধ্যে বৃহত্তম নির্ণয়",
            subtitleEn = "Largest Among Three Numbers",
            formulaTitleBn = "তুলনামূলক শর্ত",
            formulaText = "যদি (a >= b && a >= c) → a বৃহত্তম\nনতুবা যদি (b >= c) → b বৃহত্তম\nঅন্যথায় → c বৃহত্তম",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি a = 15, b = 42, c = 28)",
            mathStepsBn = listOf(
                "ধাপ ১: a >= b && a >= c অর্থাৎ 15 >= 42 (মিথ্যা)",
                "ধাপ ২: b >= c অর্থাৎ 42 >= 28 (সত্য)",
                "ফলাফল: বৃহত্তম সংখ্যা = 42 (b)"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: তিনটি সংখ্যা a, b, c ইনপুট নিই।",
                "ধাপ ৩: যদি a >= b এবং a >= c হয়, তবে \"a বৃহত্তম\" প্রিন্ট করি।",
                "ধাপ ৪: অন্যথায় যদি b >= c হয়, তবে \"b বৃহত্তম\" প্রিন্ট করি।",
                "ধাপ ৫: তা না হলে \"c বৃহত্তম\" প্রিন্ট করি।",
                "ধাপ ৬: শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.TwoBranchDecision(
                startLabel = "Start (শুরু)",
                inputLabel = "Input a, b, c",
                conditionLabel = "a >= b && a >= c ?",
                yesBranchLabel = "Print a (Largest)",
                noBranchLabel = "If b >= c Print b\nElse Print c",
                endLabel = "Stop (শেষ)"
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    int a, b, c;
                    scanf("%d %d %d", &a, &b, &c);
                    if (a >= b && a >= c) {
                        printf("Largest = %d\n", a);
                    } else if (b >= c) {
                        printf("Largest = %d\n", b);
                    } else {
                        printf("Largest = %d\n", c);
                    }
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "15 42 28"
        ),

        // 7. Leap Year
        AlgorithmFlowchartTopic(
            id = "algo_leap_year",
            category = AlgoTopicCategory.DECISION,
            titleBn = "অধিবর্ষ (Leap Year) নির্ণয়",
            subtitleEn = "Leap Year Verification",
            formulaTitleBn = "অধিবর্ষের গাণিতিক শর্ত",
            formulaText = "(y % 400 == 0)  OR  (y % 4 == 0 && y % 100 != 0)",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (y = 2024 এবং y = 1900)",
            mathStepsBn = listOf(
                "y = 2024: 2024 % 4 = 0 এবং 2024 % 100 = 24 ≠ 0 → অধিবর্ষ (Leap Year)",
                "y = 1900: 1900 % 400 = 300 ≠ 0 এবং 1900 % 100 = 0 → অধিবর্ষ নয় (Not Leap Year)",
                "y = 2000: 2000 % 400 = 0 → অধিবর্ষ (Leap Year)"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: বছর y ইনপুট নিই।",
                "ধাপ ৩: শর্ত যাচাই করি: (y % 400 == 0) অথবা (y % 4 == 0 && y % 100 != 0)।",
                "ধাপ ৪: সত্য হলে \"Leap Year\" এবং মিথ্যা হলে \"Not Leap Year\" প্রিন্ট করি।",
                "ধাপ ৫: শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.TwoBranchDecision(
                startLabel = "Start (শুরু)",
                inputLabel = "Input year y",
                conditionLabel = "(y%400==0) ||\n(y%4==0 && y%100!=0)?",
                yesBranchLabel = "Print \"Leap Year\"",
                noBranchLabel = "Print \"Not Leap Year\"",
                endLabel = "Stop (শেষ)"
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    int y;
                    scanf("%d", &y);
                    if ((y % 400 == 0) || (y % 4 == 0 && y % 100 != 0)) {
                        printf("%d is Leap Year\n", y);
                    } else {
                        printf("%d is Not Leap Year\n", y);
                    }
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "2024"
        ),

        // 8. Quadratic Equation Roots
        AlgorithmFlowchartTopic(
            id = "algo_quadratic",
            category = AlgoTopicCategory.DECISION,
            titleBn = "দ্বিঘাত সমীকরণের মূল নির্ণয়",
            subtitleEn = "Roots of Quadratic Equation (ax² + bx + c = 0)",
            formulaTitleBn = "নিশ্চায়ক (Discriminant) ও মূলের সূত্র",
            formulaText = "D = b² - 4ac\nx₁ = (-b + √D) / (2a),   x₂ = (-b - √D) / (2a)",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি a = 1, b = -5, c = 6)",
            mathStepsBn = listOf(
                "সমীকরণ: x² - 5x + 6 = 0",
                "নিশ্চায়ক, D = (-5)² - 4×1×6 = 25 - 24 = 1 (যেহেতু D >= 0, বাস্তব মূল বিদ্যমান)",
                "x₁ = (5 + √1) ÷ (2×1) = (5 + 1) ÷ 2 = 3.00",
                "x₂ = (5 - √1) ÷ (2×1) = (5 - 1) ÷ 2 = 2.00"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: সহগ a, b, c এর মান ইনপুট নিই।",
                "ধাপ ৩: নিশ্চায়ক D = b*b - 4*a*c নির্ণয় করি।",
                "ধাপ ৪: যদি D >= 0 হয় তবে x1 = (-b + √D)/(2a) এবং x2 = (-b - √D)/(2a) বের করে প্রিন্ট করি।",
                "ধাপ ৫: যদি D < 0 হয় তবে \"Imaginary Roots\" প্রিন্ট করি।",
                "ধাপ ৬: শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.TwoBranchDecision(
                startLabel = "Start (শুরু)",
                inputLabel = "Input a, b, c",
                preProcessLabel = "D = b*b - 4*a*c",
                conditionLabel = "Is D >= 0 ?",
                yesBranchLabel = "x1=(-b+√D)/(2a)\nx2=(-b-√D)/(2a)\nPrint x1, x2",
                noBranchLabel = "Print \"Imaginary\nRoots\"",
                endLabel = "Stop (শেষ)"
            ),
            cCode = """
                #include <stdio.h>
                #include <math.h>
                int main() {
                    float a, b, c, d, x1, x2;
                    scanf("%f %f %f", &a, &b, &c);
                    d = b * b - 4 * a * c;
                    if (d >= 0) {
                        x1 = (-b + sqrt(d)) / (2 * a);
                        x2 = (-b - sqrt(d)) / (2 * a);
                        printf("x1 = %.2f, x2 = %.2f\n", x1, x2);
                    } else {
                        printf("Imaginary Roots\n");
                    }
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "1 -5 6"
        ),

        // 9. Natural Number Series Sum: 1 + 2 + ... + n
        AlgorithmFlowchartTopic(
            id = "algo_series_sum",
            category = AlgoTopicCategory.LOOP_SERIES,
            titleBn = "সমান্তর ধারার যোগফল (1 + 2 + ... + n)",
            subtitleEn = "Sum of Natural Series using Loop",
            formulaTitleBn = "ধারা ও গাণিতিক সূত্র",
            formulaText = "লুপ: sum = sum + i (i = 1 থেকে n)\nসরাসরি ম্যাথ সূত্র: Sum = n(n + 1) ÷ 2",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি n = 5)",
            mathStepsBn = listOf(
                "শুরুতে sum = 0, i = 1",
                "ধাপ i=1: sum = 0 + 1 = 1 | ধাপ i=2: sum = 1 + 2 = 3",
                "ধাপ i=3: sum = 3 + 3 = 6 | ধাপ i=4: sum = 6 + 4 = 10",
                "ধাপ i=5: sum = 10 + 5 = 15 (যাচাই: 5×6÷2 = 15)"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: শেষ পদ n এর মান ইনপুট নিই।",
                "ধাপ ৩: sum = 0 এবং কাউন্টার i = 1 ধরি।",
                "ধাপ ৪: যদি i <= n হয়, তবে sum = sum + i এবং i = i + 1 করে ধাপ ৪-এ ফিরে যাই।",
                "ধাপ ৫: শর্ত মিথ্যা হলে sum প্রিন্ট করি।",
                "ধাপ ৬: শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.LoopFlow(
                startLabel = "Start (শুরু)",
                inputLabel = "Input n",
                initLabel = "sum = 0, i = 1",
                conditionLabel = "Is i <= n ?",
                bodyProcessLabel = "sum = sum + i",
                updateLabel = "i = i + 1",
                outputLabel = "Print sum",
                endLabel = "Stop (শেষ)"
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    int n, i, sum = 0;
                    scanf("%d", &n);
                    for (i = 1; i <= n; i++) {
                        sum = sum + i;
                    }
                    printf("Sum = %d\n", sum);
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "5"
        ),

        // 10. Square Series: 1^2 + 2^2 + ... + n^2
        AlgorithmFlowchartTopic(
            id = "algo_square_series",
            category = AlgoTopicCategory.LOOP_SERIES,
            titleBn = "বর্গ ধারার যোগফল (1² + 2² + ... + n²)",
            subtitleEn = "Sum of Square Series",
            formulaTitleBn = "বর্গ ধারার সূত্র",
            formulaText = "লুপ: sum = sum + (i × i)\nগাণিতিক সূত্র: Sum = n(n + 1)(2n + 1) ÷ 6",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি n = 4)",
            mathStepsBn = listOf(
                "ধারা: 1² + 2² + 3² + 4² = 1 + 4 + 9 + 16",
                "লুপ ট্রেস: sum = 0 → 1 → 5 → 14 → 30",
                "সূত্র যাচাই: 4 × (4+1) × (8+1) ÷ 6 = 4 × 5 × 9 ÷ 6 = 180 ÷ 6 = 30"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: পদসংখ্যা n ইনপুট নিই।",
                "ধাপ ৩: sum = 0, i = 1 সেট করি।",
                "ধাপ ৪: i <= n সত্য হলে sum = sum + i * i এবং i = i + 1 করি (পুনরাবৃত্তি)।",
                "ধাপ ৫: লুপ শেষে sum প্রিন্ট করি।",
                "ধাপ ৬: শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.LoopFlow(
                startLabel = "Start (শুরু)",
                inputLabel = "Input n",
                initLabel = "sum = 0, i = 1",
                conditionLabel = "Is i <= n ?",
                bodyProcessLabel = "sum = sum + (i * i)",
                updateLabel = "i = i + 1",
                outputLabel = "Print sum",
                endLabel = "Stop (শেষ)"
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    int n, i, sum = 0;
                    scanf("%d", &n);
                    for (i = 1; i <= n; i++) {
                        sum = sum + i * i;
                    }
                    printf("Square Sum = %d\n", sum);
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "4"
        ),

        // 11. Factorial of n (n!)
        AlgorithmFlowchartTopic(
            id = "algo_factorial",
            category = AlgoTopicCategory.LOOP_SERIES,
            titleBn = "n সংখ্যার ফ্যাক্টোরিয়াল (n!) নির্ণয়",
            subtitleEn = "Factorial Calculation using Loop",
            formulaTitleBn = "গাণিতিক সূত্র",
            formulaText = "n! = 1 × 2 × 3 × ... × n   (যেখানে 0! = 1)",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি n = 5)",
            mathStepsBn = listOf(
                "শুরুতে fact = 1, i = 1",
                "i=1: fact = 1 × 1 = 1 | i=2: fact = 1 × 2 = 2",
                "i=3: fact = 2 × 3 = 6 | i=4: fact = 6 × 4 = 24",
                "i=5: fact = 24 × 5 = 120 → ফলাফল 5! = 120"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: ধনাত্মক পূর্ণসংখ্যা n ইনপুট নিই।",
                "ধাপ ৩: fact = 1 এবং i = 1 ধরি।",
                "ধাপ ৪: যদি i <= n হয়, তবে fact = fact * i এবং i = i + 1 করে ধাপ ৪-এ ফিরে যাই।",
                "ধাপ ৫: লুপ শেষে fact প্রিন্ট করি।",
                "ধাপ ৬: শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.LoopFlow(
                startLabel = "Start (শুরু)",
                inputLabel = "Input n",
                initLabel = "fact = 1, i = 1",
                conditionLabel = "Is i <= n ?",
                bodyProcessLabel = "fact = fact * i",
                updateLabel = "i = i + 1",
                outputLabel = "Print fact",
                endLabel = "Stop (শেষ)"
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    int n, i;
                    long fact = 1;
                    scanf("%d", &n);
                    for (i = 1; i <= n; i++) {
                        fact = fact * i;
                    }
                    printf("Factorial = %ld\n", fact);
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "5"
        ),

        // 12. GCD of Two Numbers (Euclidean Algorithm)
        AlgorithmFlowchartTopic(
            id = "algo_gcd",
            category = AlgoTopicCategory.LOOP_SERIES,
            titleBn = "দুটি সংখ্যার গ.সা.গু (GCD) নির্ণয়",
            subtitleEn = "GCD using Euclidean Algorithm",
            formulaTitleBn = "ইউক্লিডীয় ভাগশেষ সূত্র",
            formulaText = "যতক্ষণ b ≠ 0:\n  rem = a % b,   a = b,   b = rem\nশেষে a হলো গ.সা.গু (GCD)",
            mathExampleTitleBn = "উদাহরণ ম্যাথ (ধরি a = 24, b = 18)",
            mathStepsBn = listOf(
                "ধাপ ১: a = 24, b = 18 → rem = 24 % 18 = 6; এখন a = 18, b = 6",
                "ধাপ ২: a = 18, b = 6 → rem = 18 % 6 = 0; এখন a = 6, b = 0",
                "যেহেতু b == 0, লুপ থামবে → গ.সা.গু (GCD) = 6"
            ),
            algorithmStepsBn = listOf(
                "ধাপ ১: শুরু করি (Start)।",
                "ধাপ ২: দুটি সংখ্যা a ও b ইনপুট নিই।",
                "ধাপ ৩: যদি b != 0 হয়, তবে rem = a % b, a = b এবং b = rem করে ধাপ ৩ পুনরাবৃত্তি করি।",
                "ধাপ ৪: b == 0 হলে গ.সা.গু হিসেবে a প্রিন্ট করি।",
                "ধাপ ৫: শেষ করি (Stop)।"
            ),
            flowchart = FlowchartDiagramSpec.LoopFlow(
                startLabel = "Start (শুরু)",
                inputLabel = "Input a, b",
                initLabel = "rem = 0",
                conditionLabel = "Is b != 0 ?",
                bodyProcessLabel = "rem = a % b",
                updateLabel = "a = b, b = rem",
                outputLabel = "Print GCD = a",
                endLabel = "Stop (শেষ)"
            ),
            cCode = """
                #include <stdio.h>
                int main() {
                    int a, b, rem;
                    scanf("%d %d", &a, &b);
                    while (b != 0) {
                        rem = a % b;
                        a = b;
                        b = rem;
                    }
                    printf("GCD = %d\n", a);
                    return 0;
                }
            """.trimIndent(),
            sampleStdin = "24 18"
        )
    )
}
