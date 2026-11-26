package com.example.data.local

import com.example.data.model.BatchStudentSummary
import com.example.data.model.ExamQuestion
import com.example.data.model.FaqItem
import com.example.data.model.NoticeEntity
import com.example.data.model.PracticeProblemEntity

object CurriculumSeed {

    val initialProblems: List<PracticeProblemEntity> = listOf(
        // ==================== HTML5 SYLLABUS MODULES (8 Modules, 10 Problems) ====================
        PracticeProblemEntity(
            id = "html_basic_1",
            language = "HTML",
            moduleKey = "html_basic",
            moduleTitleBn = "১. বেসিক ট্যাগ",
            moduleTitleEn = "Basic Tags",
            titleBn = "প্রথম ওয়েবপেজ ও হেডিং অনুশীলন",
            titleEn = "First Webpage & Headings",
            difficulty = "Easy",
            instructionsBn = "<html>, <head>, <title>, <body>, একটি <h1> হেডিং এবং একটি <p> প্যারাগ্রাফ লিখে কলেজ পরিচিতি দেখাও।",
            starterCode = """<!DOCTYPE html>
<html>
<head>
  <title>HSC ICT Lab</title>
</head>
<body>
  <h1>আদ্-দ্বীন সায়েন্স কলেজ, রংপুর</h1>
  <p>আমি প্রতিদিন HTML প্র্যাকটিস করছি।</p>
</body>
</html>""",
            solutionCode = """<!DOCTYPE html>
<html>
<head>
  <title>HSC ICT Lab</title>
</head>
<body>
  <h1>আদ্-দ্বীন সায়েন্স কলেজ, রংপুর</h1>
  <h3>ICT প্র্যাকটিস সেশন</h3>
  <p>আমি প্রতিদিন HTML প্র্যাকটিস করছি।</p>
</body>
</html>""",
            hintBn = "<h1> সবচেয়ে বড় হেডিং আর <p> দিয়ে অনুচ্ছেদ লেখা হয়। প্রতিটি ট্যাগ সঠিকভাবে বন্ধ (</tag>) করো।",
            commonMistakeBn = "বোর্ড পরীক্ষায় অনেকেই <title> ট্যাগ <head>-এর ভেতরে না লিখে <body>-তে লেখে বা </title> ক্লোজ করতে ভুলে যায়!",
            requiredTagsCsv = "html,head,title,body,h1,p",
            sortOrder = 1
        ),
        PracticeProblemEntity(
            id = "html_format_1",
            language = "HTML",
            moduleKey = "html_formatting",
            moduleTitleBn = "২. টেক্সট ফরম্যাটিং",
            moduleTitleEn = "Formatting",
            titleBn = "রাসায়নিক ও গাণিতিক সংকেত (sub/sup)",
            titleEn = "Chemical & Math Formula (sub/sup)",
            difficulty = "Board",
            instructionsBn = "HTML ফরম্যাটিং ট্যাগ ব্যবহার করে H₂SO₄ এবং (a+b)² = a² + 2ab + b² আউটপুটে দেখাও।",
            starterCode = """<!DOCTYPE html>
<html>
<body>
  <h3>ফরম্যাটিং অনুশীলন</h3>
  <p>সালফিউরিক এসিড: H<sub>2</sub>SO<sub>4</sub></p>
  <p>সূত্র: (a+b)<sup>2</sup> = a<sup>2</sup> + 2ab + b<sup>2</sup></p>
</body>
</html>""",
            solutionCode = """<!DOCTYPE html>
<html>
<body>
  <h3>ফরম্যাটিং অনুশীলন</h3>
  <p><b>সালফিউরিক এসিড:</b> H<sub>2</sub>SO<sub>4</sub></p>
  <p><i>সূত্র:</i> (a+b)<sup>2</sup> = a<sup>2</sup> + 2ab + b<sup>2</sup></p>
</body>
</html>""",
            hintBn = "নিচে লিখতে <sub> এবং উপরে পাওয়ার লিখতে <sup> ট্যাগ ব্যবহার হয়।",
            commonMistakeBn = "অনেক শিক্ষার্থী <sub> ও <sup> গুলিয়ে ফেলে। মনে রাখো: Sub = নিচে (Submarine), Sup = উপরে (Super)!",
            requiredTagsCsv = "sub,sup,p",
            sortOrder = 2
        ),
        PracticeProblemEntity(
            id = "html_list_1",
            language = "HTML",
            moduleKey = "html_lists",
            moduleTitleBn = "৩. লিস্ট (UL/OL)",
            moduleTitleEn = "Lists",
            titleBn = "Order ও Unordered List তৈরি",
            titleEn = "Ordered & Unordered Lists",
            difficulty = "Easy",
            instructionsBn = "একটি <ol> (Ordered List) এ তিনটি বিষয়ের নাম এবং একটি <ul> এ দুটি প্রোগ্রামিং ভাষার নাম দেখাও।",
            starterCode = """<!DOCTYPE html>
<html>
<body>
  <h3>HSC বিজ্ঞান বিষয়সমূহ</h3>
  <ol type="A">
    <li>ICT</li>
    <li>Physics</li>
    <li>Chemistry</li>
  </ol>
  <h3>প্রোগ্রামিং</h3>
  <ul>
    <li>HTML</li>
    <li>C Language</li>
  </ul>
</body>
</html>""",
            solutionCode = """<!DOCTYPE html>
<html>
<body>
  <h3>HSC বিজ্ঞান বিষয়সমূহ</h3>
  <ol type="1">
    <li>ICT</li>
    <li>Physics</li>
    <li>Chemistry</li>
  </ol>
  <h3>প্রোগ্রামিং</h3>
  <ul type="square">
    <li>HTML</li>
    <li>C Language</li>
  </ul>
</body>
</html>""",
            hintBn = "তালিকার প্রতিটি আইটেম অবশ্যই <li>...</li> এর ভেতরে রাখতে হবে।",
            commonMistakeBn = "<ul> বা <ol>-এর ভেতরে <li> ছাড়া সরাসরি টেক্সট লেখা ভুল; বোর্ড পরীক্ষায় type=\"square\" বা \"A\" এট্রিবিউট প্রায়ই আসে।",
            requiredTagsCsv = "ol,ul,li",
            sortOrder = 3
        ),
        PracticeProblemEntity(
            id = "html_table_1",
            language = "HTML",
            moduleKey = "html_tables",
            moduleTitleBn = "৪. টেবিল (Table)",
            moduleTitleEn = "Tables",
            titleBn = "বোর্ড স্ট্যান্ডার্ড স্টুডেন্ট মার্কস টেবিল",
            titleEn = "Student Marks Table with Colspan",
            difficulty = "Board",
            instructionsBn = "border=\"1\" সহ একটি টেবিল তৈরি করো যেখানে প্রথম সারিতে colspan=\"2\" দিয়ে 'Student Result' হেডার থাকবে।",
            starterCode = """<!DOCTYPE html>
<html>
<body>
  <table border="1" cellpadding="6" cellspacing="0">
    <tr>
      <th colspan="2">Student Result</th>
    </tr>
    <tr>
      <th>Name</th>
      <th>Grade</th>
    </tr>
    <tr>
      <td>Karim</td>
      <td>A+</td>
    </tr>
  </table>
</body>
</html>""",
            solutionCode = """<!DOCTYPE html>
<html>
<body>
  <table border="1" cellpadding="6" cellspacing="0">
    <caption>HSC ICT Result</caption>
    <tr>
      <th colspan="2">Student Result</th>
    </tr>
    <tr>
      <th>Name</th>
      <th>Grade</th>
    </tr>
    <tr>
      <td>Karim</td>
      <td>A+</td>
    </tr>
  </table>
</body>
</html>""",
            hintBn = "কলাম একত্র করতে colspan আর সারি একত্র করতে rowspan ব্যবহার করো।",
            commonMistakeBn = "সবচেয়ে বেশি নম্বর কাটা যায় <tr> বন্ধ না করলে বা colspan ও rowspan উল্টো ব্যবহার করলে!",
            requiredTagsCsv = "table,tr,th,td,colspan",
            sortOrder = 4
        ),
        PracticeProblemEntity(
            id = "html_link_1",
            language = "HTML",
            moduleKey = "html_links",
            moduleTitleBn = "৫. হাইপারলিংক",
            moduleTitleEn = "Hyperlinks",
            titleBn = "বহিরাগত ওয়েবসাইট লিংক (<a> ট্যাগ)",
            titleEn = "External Hyperlink with Target",
            difficulty = "Easy",
            instructionsBn = "<a> ট্যাগ ও href এট্রিবিউট ব্যবহার করে একটি ক্লিকেবল লিংক তৈরি করো যা নতুন ট্যাবে (target=\"_blank\") খুলবে।",
            starterCode = """<!DOCTYPE html>
<html>
<body>
  <h3>শিক্ষা বোর্ড লিংক</h3>
  <p>ফলাফল জানতে নিচের লিংকে ক্লিক করো:</p>
  <a href="https://www.educationboardresults.gov.bd" target="_blank">
    Education Board Result
  </a>
</body>
</html>""",
            solutionCode = """<!DOCTYPE html>
<html>
<body>
  <h3>শিক্ষা বোর্ড লিংক</h3>
  <a href="https://www.educationboardresults.gov.bd" target="_blank">Education Board Result</a>
</body>
</html>""",
            hintBn = "লিংকের ঠিকানা অবশ্যই href=\"...\" এট্রিবিউটে এবং লিংকের লেখাটি <a> ও </a>-এর মাঝে দিতে হবে।",
            commonMistakeBn = "অনেকে href এর বদলে src লেখে বা http:// / https:// বাদ দিয়ে দেয়।",
            requiredTagsCsv = "a,href",
            sortOrder = 5
        ),
        PracticeProblemEntity(
            id = "html_img_1",
            language = "HTML",
            moduleKey = "html_images",
            moduleTitleBn = "৬. ইমেজ (Image)",
            moduleTitleEn = "Images",
            titleBn = "ছবি প্রদর্শন ও alt এট্রিবিউট",
            titleEn = "Image Tag & Alt Attribute",
            difficulty = "Easy",
            instructionsBn = "<img> ট্যাগে src, alt, width এবং height এট্রিবিউট ব্যবহার করো। (লাইভ প্রিভিউতে SVG ডাটা URI দিয়ে দ্রুত ছবি দেখা যায়)।",
            starterCode = """<!DOCTYPE html>
<html>
<body>
  <h3>কলেজ ব্যানার</h3>
  <img src="data:image/svg+xml;utf8,<svg xmlns='http://www.w3.org/2000/svg' width='220' height='100'><rect width='220' height='100' rx='14' fill='%230F766E'/><text x='50%' y='55%' fill='white' font-size='16' text-anchor='middle'>HSC ICT A+</text></svg>"
       alt="HSC ICT Banner"
       width="220"
       height="100">
</body>
</html>""",
            solutionCode = """<!DOCTYPE html>
<html>
<body>
  <h3>কলেজ ব্যানার</h3>
  <img src="college.jpg" alt="Addin Science College" width="220" height="100">
</body>
</html>""",
            hintBn = "<img> একটি Empty Tag—এর কোনো </img> ক্লোজিং ট্যাগ নেই।",
            commonMistakeBn = "বোর্ড পরীক্ষায় </img> লিখলে নম্বর কাটা যায় এবং ছবি না লোড হলে alt টেক্সট দেখায়—তাই alt জরুরি।",
            requiredTagsCsv = "img,src,alt",
            sortOrder = 6
        ),
        PracticeProblemEntity(
            id = "html_form_1",
            language = "HTML",
            moduleKey = "html_forms",
            moduleTitleBn = "৭. ফর্ম (Form)",
            moduleTitleEn = "Forms",
            titleBn = "শিক্ষার্থী নিবন্ধন ফর্ম",
            titleEn = "Student Registration Form",
            difficulty = "Medium",
            instructionsBn = "<form>, <label>, <input type=\"text\">, রেডিও বাটন এবং Submit <button> দিয়ে একটি পরিষ্কার ফর্ম বানাও।",
            starterCode = """<!DOCTYPE html>
<html>
<body>
  <h3>ভর্তি ফর্ম</h3>
  <form>
    <label>নাম:</label><br>
    <input type="text" placeholder="তোমার নাম লেখো"><br><br>
    <label>বিভাগ:</label><br>
    <input type="radio" name="group" checked> Science
    <input type="radio" name="group"> Humanities<br><br>
    <button type="submit">জমা দাও</button>
  </form>
</body>
</html>""",
            solutionCode = """<!DOCTYPE html>
<html>
<body>
  <h3>ভর্তি ফর্ম</h3>
  <form>
    <label>নাম:</label><br>
    <input type="text" placeholder="তোমার নাম লেখো"><br><br>
    <button type="submit">জমা দাও</button>
  </form>
</body>
</html>""",
            hintBn = "একই গ্রুপের রেডিও বাটনের জন্য name এট্রিবিউট একই হতে হয়।",
            commonMistakeBn = "রেডিও বাটনে আলাদা name দিলে একসাথে সবগুলো সিলেক্ট হয়ে যায়!",
            requiredTagsCsv = "form,input,label,button",
            sortOrder = 7
        ),
        PracticeProblemEntity(
            id = "html_layout_1",
            language = "HTML",
            moduleKey = "html_layout",
            moduleTitleBn = "৮. লেআউট ও CSS",
            moduleTitleEn = "Layout & CSS",
            titleBn = "CSS কার্ড ও স্টাইলিং বেসিক",
            titleEn = "Div Card & CSS Styling",
            difficulty = "Medium",
            instructionsBn = "<style> বা inline style ও <div> ব্যবহার করে রঙিন একটি নোট কার্ড তৈরি করো।",
            starterCode = """<!DOCTYPE html>
<html>
<head>
  <style>
    .card {
      background-color: #CCFBF1;
      border-left: 5px solid #0F766E;
      padding: 14px;
      border-radius: 10px;
      font-family: sans-serif;
    }
  </style>
</head>
<body>
  <div class="card">
    <h3 style="color:#0F766E;">CSS নোট কার্ড</h3>
    <p>HTML ও CSS মিলে সুন্দর ওয়েব ডিজাইন তৈরি হয়।</p>
  </div>
</body>
</html>""",
            solutionCode = """<!DOCTYPE html>
<html>
<head>
  <style>
    .card { background: #CCFBF1; padding: 14px; border-radius: 10px; }
  </style>
</head>
<body>
  <div class="card"><h3>CSS নোট কার্ড</h3><p>ডিজাইন সম্পন্ন!</p></div>
</body>
</html>""",
            hintBn = "CSS এ প্রতিটি প্রোপার্টির শেষে সেমিকোলন (;) দিতে ভুলবে না।",
            commonMistakeBn = "স্টাইল এট্রিবিউটে = এর বদলে : ব্যবহার করতে হয় (যেমন color: red;)।",
            requiredTagsCsv = "div,style",
            sortOrder = 8
        ),

        // ==================== C PROGRAMMING SYLLABUS MODULES (8 Modules, 10 Problems) ====================
        PracticeProblemEntity(
            id = "c_io_1",
            language = "C",
            moduleKey = "c_io",
            moduleTitleBn = "১. ডাটা টাইপ ও printf",
            moduleTitleEn = "Data Types & I/O",
            titleBn = "ভেরিয়েবল ও printf আউটপুট",
            titleEn = "Variables & printf",
            difficulty = "Easy",
            instructionsBn = "দুটি পূর্ণসংখ্যা a = 15 ও b = 25 যোগ করে ফলাফল প্রিন্ট করো।",
            starterCode = """#include <stdio.h>

int main() {
    int a = 15, b = 25;
    int sum = a + b;
    printf("Sum = %d\n", sum);
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int main() {
    int a = 15, b = 25;
    int sum = a + b;
    printf("Sum = %d\n", sum);
    return 0;
}""",
            hintBn = "পূর্ণসংখ্যার জন্য %d এবং দশমিক সংখ্যার জন্য %f ফরম্যাট স্পেসিফায়ার ব্যবহার হয়।",
            commonMistakeBn = "প্রায় সব শিক্ষার্থী লাইনের শেষে সেমিকোলন (;) বা main()-এ return 0; লিখতে ভুলে যায়।",
            testInput = "",
            expectedOutput = "Sum = 40",
            sortOrder = 9
        ),
        PracticeProblemEntity(
            id = "c_scan_1",
            language = "C",
            moduleKey = "c_operators",
            moduleTitleBn = "২. ইনপুট ও অপারেটর",
            moduleTitleEn = "scanf & Operators",
            titleBn = "ত্রিভুজের ক্ষেত্রফল নির্ণয়",
            titleEn = "Area of Triangle (scanf)",
            difficulty = "Board",
            instructionsBn = "ভূমি (base) ও উচ্চতা (height) ইনপুট নিয়ে ত্রিভুজের ক্ষেত্রফল (0.5 * base * height) দুই দশমিক স্থান পর্যন্ত দেখাও। ইনপুট বক্সে `10 5` দাও।",
            starterCode = """#include <stdio.h>

int main() {
    float base, height, area;
    scanf("%f %f", &base, &height);
    area = 0.5 * base * height;
    printf("Area = %.2f\n", area);
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int main() {
    float base, height, area;
    scanf("%f %f", &base, &height);
    area = 0.5 * base * height;
    printf("Area = %.2f\n", area);
    return 0;
}""",
            hintBn = "scanf()-এ ভেরিয়েবলের আগে অবশ্যই & (Address-of operator) দিতে হবে।",
            commonMistakeBn = "বোর্ড পরীক্ষায় 1/2 লিখলে integer division হয়ে 0 আসে! তাই সবসময় 0.5 বা 1.0/2.0 লিখবে।",
            testInput = "10 5",
            expectedOutput = "Area = 25.00",
            sortOrder = 10
        ),
        PracticeProblemEntity(
            id = "c_ifelse_1",
            language = "C",
            moduleKey = "c_conditions",
            moduleTitleBn = "৩. শর্ত (if-else)",
            moduleTitleEn = "Conditionals",
            titleBn = "জোড় না বিজোড় সংখ্যা নির্ণয়",
            titleEn = "Even or Odd Number",
            difficulty = "Board",
            instructionsBn = "একটি সংখ্যা ইনপুট নিয়ে সেটি Even না Odd তা প্রিন্ট করো। টেস্ট ইনপুট: 14",
            starterCode = """#include <stdio.h>

int main() {
    int n;
    scanf("%d", &n);
    if (n % 2 == 0) {
        printf("Even\n");
    } else {
        printf("Odd\n");
    }
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int main() {
    int n;
    scanf("%d", &n);
    if (n % 2 == 0) {
        printf("Even\n");
    } else {
        printf("Odd\n");
    }
    return 0;
}""",
            hintBn = "ভাগশেষ বের করতে % (Modulus) অপারেটর এবং সমান যাচাই করতে == ব্যবহার করো।",
            commonMistakeBn = "if (n % 2 = 0) লিখলে তুলনা না হয়ে অ্যাসাইনমেন্ট হয়! শর্তের ভেতরে == দিতে হবে।",
            testInput = "14",
            expectedOutput = "Even",
            sortOrder = 11
        ),
        PracticeProblemEntity(
            id = "c_ifelse_2",
            language = "C",
            moduleKey = "c_conditions",
            moduleTitleBn = "৩. শর্ত (if-else)",
            moduleTitleEn = "Conditionals",
            titleBn = "অধিবর্ষ (Leap Year) যাচাই",
            titleEn = "Leap Year Check",
            difficulty = "Board",
            instructionsBn = "একটি সাল ইনপুট নিয়ে সেটি Leap Year কি না তা নির্ণয় করো। টেস্ট ইনপুট: 2024",
            starterCode = """#include <stdio.h>

int main() {
    int y;
    scanf("%d", &y);
    if ((y % 400 == 0) || (y % 4 == 0 && y % 100 != 0)) {
        printf("Leap Year\n");
    } else {
        printf("Not Leap Year\n");
    }
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int main() {
    int y;
    scanf("%d", &y);
    if ((y % 400 == 0) || (y % 4 == 0 && y % 100 != 0)) {
        printf("Leap Year\n");
    } else {
        printf("Not Leap Year\n");
    }
    return 0;
}""",
            hintBn = "শতাব্দী বছর হলে ৪০০ দিয়ে, অন্যথায় ৪ দিয়ে বিভাজ্য ও ১০০ দিয়ে অবিভাজ্য হতে হবে।",
            commonMistakeBn = "শুধু y % 4 == 0 লিখলে 1900 সালকেও ভুল করে Leap Year দেখাবে!",
            testInput = "2024",
            expectedOutput = "Leap Year",
            sortOrder = 12
        ),
        PracticeProblemEntity(
            id = "c_switch_1",
            language = "C",
            moduleKey = "c_switch",
            moduleTitleBn = "৪. সুইচ-কেস (switch)",
            moduleTitleEn = "Switch-Case",
            titleBn = "Switch দিয়ে গ্রেড নির্ণয়",
            titleEn = "Grade Finder with Switch",
            difficulty = "Medium",
            instructionsBn = "নম্বর (০-১০০) ইনপুট নিয়ে mark / 10 অনুযায়ী Grade প্রিন্ট করো। টেস্ট ইনপুট: 85",
            starterCode = """#include <stdio.h>

int main() {
    int mark;
    scanf("%d", &mark);
    switch (mark / 10) {
        case 10:
        case 9:
        case 8:
            printf("Grade: A+\n");
            break;
        case 7:
            printf("Grade: A\n");
            break;
        case 6:
            printf("Grade: A-\n");
            break;
        default:
            printf("Keep Practicing\n");
    }
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int main() {
    int mark;
    scanf("%d", &mark);
    switch (mark / 10) {
        case 10:
        case 9:
        case 8:
            printf("Grade: A+\n");
            break;
        case 7:
            printf("Grade: A\n");
            break;
        default:
            printf("Keep Practicing\n");
    }
    return 0;
}""",
            hintBn = "প্রতিটি case ব্লকের শেষে break; না দিলে নিচের case গুলোও রান হয়ে যাবে।",
            commonMistakeBn = "break; দিতে ভুলে যাওয়া (Fall-through bug) switch-case এর সবচেয়ে সাধারণ ভুল।",
            testInput = "85",
            expectedOutput = "Grade: A+",
            sortOrder = 13
        ),
        PracticeProblemEntity(
            id = "c_loop_1",
            language = "C",
            moduleKey = "c_loops",
            moduleTitleBn = "৫. লুপ (for/while)",
            moduleTitleEn = "Loops",
            titleBn = "১ থেকে N পর্যন্ত ধারার যোগফল",
            titleEn = "Sum of Series 1+2+...+N",
            difficulty = "Board",
            instructionsBn = "for লুপ ব্যবহার করে 1 + 2 + 3 + ... + N ধারার যোগফল বের করো। Dry-Run মোডে প্রতিটি ধাপের মান দেখো! টেস্ট ইনপুট: 5",
            starterCode = """#include <stdio.h>

int main() {
    int n, i, sum = 0;
    scanf("%d", &n);
    for (i = 1; i <= n; i++) {
        sum = sum + i;
    }
    printf("Sum = %d\n", sum);
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int main() {
    int n, i, sum = 0;
    scanf("%d", &n);
    for (i = 1; i <= n; i++) {
        sum = sum + i;
    }
    printf("Sum = %d\n", sum);
    return 0;
}""",
            hintBn = "যোগফল রাখার ভেরিয়েবল sum অবশ্যই 0 দিয়ে শুরু করতে হবে (sum = 0)।",
            commonMistakeBn = "sum = 0 না লিখলে মেমোরির গারবেজ ভ্যালু যুক্ত হয়ে ভুল উত্তর আসে, আর for(...) এর পরে সেমিকোলন দিলে লুপ খালি ঘোরে!",
            testInput = "5",
            expectedOutput = "Sum = 15",
            sortOrder = 14
        ),
        PracticeProblemEntity(
            id = "c_loop_2",
            language = "C",
            moduleKey = "c_loops",
            moduleTitleBn = "৫. লুপ (for/while)",
            moduleTitleEn = "Loops",
            titleBn = "ফ্যাক্টরিয়াল (Factorial) নির্ণয়",
            titleEn = "Factorial using Loop",
            difficulty = "Board",
            instructionsBn = "while বা for লুপ দিয়ে N! নির্ণয় করো। টেস্ট ইনপুট: 5",
            starterCode = """#include <stdio.h>

int main() {
    int n, i = 1, fact = 1;
    scanf("%d", &n);
    while (i <= n) {
        fact = fact * i;
        i++;
    }
    printf("Factorial = %d\n", fact);
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int main() {
    int n, i = 1, fact = 1;
    scanf("%d", &n);
    while (i <= n) {
        fact = fact * i;
        i++;
    }
    printf("Factorial = %d\n", fact);
    return 0;
}""",
            hintBn = "গুণ করার সময় fact = 1 দিয়ে শুরু করতে হয় (0 দিয়ে নয়)।",
            commonMistakeBn = "while লুপের ভেতরে i++ লিখতে ভুলে গেলে প্রোগ্রামটি অসীম (Infinite) লুপে আটকে যাবে!",
            testInput = "5",
            expectedOutput = "Factorial = 120",
            sortOrder = 15
        ),
        PracticeProblemEntity(
            id = "c_array_1",
            language = "C",
            moduleKey = "c_arrays",
            moduleTitleBn = "৬. অ্যারে (Array)",
            moduleTitleEn = "Arrays",
            titleBn = "অ্যারের সর্বোচ্চ সংখ্যা নির্ণয়",
            titleEn = "Maximum Element in Array",
            difficulty = "Medium",
            instructionsBn = "৫টি সংখ্যার অ্যারে থেকে সবচেয়ে বড় সংখ্যাটি খুঁজে বের করো।",
            starterCode = """#include <stdio.h>

int main() {
    int arr[5] = {12, 45, 7, 89, 23};
    int max = arr[0];
    int i;
    for (i = 1; i < 5; i++) {
        if (arr[i] > max) {
            max = arr[i];
        }
    }
    printf("Max = %d\n", max);
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int main() {
    int arr[5] = {12, 45, 7, 89, 23};
    int max = arr[0];
    int i;
    for (i = 1; i < 5; i++) {
        if (arr[i] > max) {
            max = arr[i];
        }
    }
    printf("Max = %d\n", max);
    return 0;
}""",
            hintBn = "অ্যারের ইনডেক্স ০ থেকে শুরু হয় (arr[0] থেকে arr[4])।",
            commonMistakeBn = "i <= 5 লিখলে অ্যারের সীমার বাইরে (Index Out of Bounds) অ্যাক্সেস হয়ে যায়।",
            testInput = "",
            expectedOutput = "Max = 89",
            sortOrder = 16
        ),
        PracticeProblemEntity(
            id = "c_func_1",
            language = "C",
            moduleKey = "c_functions",
            moduleTitleBn = "৭. ফাংশন (Function)",
            moduleTitleEn = "Functions",
            titleBn = "রিকার্সিভ ফাংশনে ফ্যাক্টরিয়াল",
            titleEn = "Recursive Function",
            difficulty = "Medium",
            instructionsBn = "নিজে একটি ফাংশন লিখে main() থেকে কল করো এবং ফলাফল দেখাও। টেস্ট ইনপুট: 6",
            starterCode = """#include <stdio.h>

int square(int x) {
    return x * x;
}

int main() {
    int n;
    scanf("%d", &n);
    printf("Square = %d\n", square(n));
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int square(int x) {
    return x * x;
}

int main() {
    int n;
    scanf("%d", &n);
    printf("Square = %d\n", square(n));
    return 0;
}""",
            hintBn = "main()-এর উপরে ফাংশন লিখলে আলাদা প্রোটোটাইপ লাগে না।",
            commonMistakeBn = "int রিটার্ন টাইপের ফাংশন থেকে return স্টেটমেন্ট দিতে ভুলে যাওয়া।",
            testInput = "6",
            expectedOutput = "Square = 36",
            sortOrder = 17
        ),
        PracticeProblemEntity(
            id = "c_board_1",
            language = "C",
            moduleKey = "c_board",
            moduleTitleBn = "৮. বোর্ড সাজেশন",
            moduleTitleEn = "Board Patterns",
            titleBn = "সেলসিয়াস থেকে ফারেনহাইট রূপান্তর",
            titleEn = "Celsius to Fahrenheit",
            difficulty = "Board",
            instructionsBn = "সেলসিয়াস তাপমাত্রা C ইনপুট নিয়ে F = (9.0 * C / 5.0) + 32 সূত্র দিয়ে ফারেনহাইট দেখাও। টেস্ট ইনপুট: 37",
            starterCode = """#include <stdio.h>

int main() {
    float c, f;
    scanf("%f", &c);
    f = (9.0 * c / 5.0) + 32;
    printf("F = %.2f\n", f);
    return 0;
}""",
            solutionCode = """#include <stdio.h>

int main() {
    float c, f;
    scanf("%f", &c);
    f = (9.0 * c / 5.0) + 32;
    printf("F = %.2f\n", f);
    return 0;
}""",
            hintBn = "9/5 এর বদলে 9.0/5.0 লিখলে সঠিক দশমিক মান পাওয়া যায়।",
            commonMistakeBn = "9/5 লিখলে C প্রোগ্রামিংয়ে পূর্ণসংখ্যার ভাগ হয়ে 1.8 এর পরিবর্তে শুধু 1 আসে!",
            testInput = "37",
            expectedOutput = "F = 98.60",
            sortOrder = 18
        )
    )

    val initialNotices: List<NoticeEntity> = listOf(
        NoticeEntity(
            id = "notice_welcome",
            title = "HSC ICT A+ প্র্যাকটিস চ্যালেঞ্জ শুরু!",
            body = "প্রতিদিন অন্তত ২টি HTML এবং ২টি C প্রোগ্রামিং সমস্যা সমাধান করো। অফলাইনেও সব লেসন ও কম্পাইলার চালু থাকবে। — সালমান শাহীন",
            tag = "Notice",
            pinned = true,
            createdAt = System.currentTimeMillis() - 86_400_000L
        ),
        NoticeEntity(
            id = "notice_board_tip",
            title = "বোর্ড পরীক্ষার জরুরি টিপস (Table & Loop)",
            body = "সৃজনশীল প্রশ্নে HTML টেবিলের colspan/rowspan এবং C ভাষার for লুপের ধারা (1+2+...+N) সবচেয়ে বেশি আসে।",
            tag = "Exam Tip",
            pinned = false,
            createdAt = System.currentTimeMillis() - 43_200_000L
        )
    )

    val examQuestions: List<ExamQuestion> = listOf(
        ExamQuestion(
            id = "q1",
            category = "HTML",
            questionBn = "HTML-এ কোনটি Empty (ফাঁকা) ট্যাগ হিসেবে পরিচিত?",
            options = listOf("<p>", "<table>", "<br>", "<h1>"),
            correctIndex = 2,
            explanationBn = "<br> একটি Empty ট্যাগ কারণ এর কোনো ক্লোজিং ট্যাগ (</br>) থাকে না।"
        ),
        ExamQuestion(
            id = "q2",
            category = "HTML",
            questionBn = "টেবিলের দুটি পাশাপাশি কলাম একত্র করতে কোন এট্রিবিউট ব্যবহার হয়?",
            options = listOf("rowspan", "colspan", "cellpadding", "merge"),
            correctIndex = 1,
            explanationBn = "কলাম একত্র করতে colspan এবং সারি একত্র করতে rowspan ব্যবহার করা হয়।"
        ),
        ExamQuestion(
            id = "q3",
            category = "HTML",
            questionBn = "H₂O প্রদর্শনের জন্য সঠিক HTML কোড কোনটি?",
            options = listOf("H<sup>2</sup>O", "H<sub>2</sub>O", "H<down>2</down>O", "H<small>2</small>O"),
            correctIndex = 1,
            explanationBn = "বেসলাইনের নিচে লিখতে <sub> (subscript) ট্যাগ ব্যবহৃত হয়।"
        ),
        ExamQuestion(
            id = "q4",
            category = "HTML",
            questionBn = "হাইপারলিংক তৈরি করতে <a> ট্যাগে কোন এট্রিবিউট বাধ্যতামূলক?",
            options = listOf("src", "link", "href", "url"),
            correctIndex = 2,
            explanationBn = "Hyperlink Reference বা href এট্রিবিউটে গন্তব্য URL লিখতে হয়।"
        ),
        ExamQuestion(
            id = "q5",
            category = "HTML",
            questionBn = "<ol type=\"i\"> লিস্টের তৃতীয় আইটেমের নম্বর কী হবে?",
            options = listOf("3", "c", "iii", "III"),
            correctIndex = 2,
            explanationBn = "type=\"i\" ছোট হাতের রোমান সংখ্যা (i, ii, iii, iv...) তৈরি করে।"
        ),
        ExamQuestion(
            id = "q6",
            category = "C",
            questionBn = "নিচের C কোডটির আউটপুট কী হবে?",
            codeSnippet = """int a = 5, b = 2;
printf("%d", a / b);""",
            options = listOf("2.5", "2", "2.0", "Error"),
            correctIndex = 1,
            explanationBn = "দুটি int সংখ্যা ভাগ করলে integer division হয়, তাই দশমিক অংশ বাদ পড়ে শুধু 2 প্রিন্ট হয়।"
        ),
        ExamQuestion(
            id = "q7",
            category = "C",
            questionBn = "C ভাষায় scanf() দিয়ে float মান ইনপুট নেওয়ার সঠিক ফরম্যাট কোনটি?",
            options = listOf("scanf(\"%d\", x);", "scanf(\"%f\", &x);", "scanf(\"%f\", x);", "scanf(\"%c\", &x);"),
            correctIndex = 1,
            explanationBn = "float ভেরিয়েবলে ইনপুট নিতে %f এবং ভেরিয়েবলের ঠিকানার জন্য & চিহ্ন দিতে হয়।"
        ),
        ExamQuestion(
            id = "q8",
            category = "C",
            questionBn = "নিচের লুপটি কতবার চলবে?",
            codeSnippet = """for (int i = 1; i <= 5; i += 2) {
    printf("%d ", i);
}""",
            options = listOf("৫ বার", "৩ বার", "২ বার", "অসীম বার"),
            correctIndex = 1,
            explanationBn = "i এর মান যথাক্রমে 1, 3 এবং 5 হবে—অর্থাৎ মোট ৩ বার লুপটি চলবে।"
        ),
        ExamQuestion(
            id = "q9",
            category = "C",
            questionBn = "C প্রোগ্রামে কোনটি বৈধ ভেরিয়েবল নাম নয়?",
            options = listOf("student_age", "_total", "2ndValue", "marks1"),
            correctIndex = 2,
            explanationBn = "ভেরিয়েবলের নামের শুরুতে অঙ্ক (digit) থাকতে পারে না, তাই 2ndValue অবৈধ।"
        ),
        ExamQuestion(
            id = "q10",
            category = "C",
            questionBn = "নিচের কোডটির আউটপুট কত?",
            codeSnippet = """int x = 10;
if (x > 5 && x < 15)
    printf("Yes");
else
    printf("No");""",
            options = listOf("Yes", "No", "10", "Compile Error"),
            correctIndex = 0,
            explanationBn = "x = 10 সংখ্যাটি 5-এর চেয়ে বড় এবং 15-এর চেয়ে ছোট—উভয় শর্ত সত্য হওয়ায় Yes প্রিন্ট হবে।"
        )
    )

    val offlineFaqs: List<FaqItem> = listOf(
        FaqItem(
            id = "faq1",
            category = "C Programming",
            questionBn = "scanf() দিয়ে ইনপুট নিলে প্রোগ্রাম ভুল মান দেখায় বা ক্র্যাশ করে কেন?",
            answerBn = "scanf()-এ ভেরিয়েবলের আগে & (address operator) দিতে ভুলে গেলে এমন হয়। যেমন: scanf(\"%d\", &n);",
            codeExample = "scanf(\"%d\", &n); // সঠিক নিয়ম"
        ),
        FaqItem(
            id = "faq2",
            category = "C Programming",
            questionBn = "9 / 5 বা 1 / 2 দিলে উত্তর সঠিক আসে না কেন?",
            answerBn = "C-তে দুটি পূর্ণসংখ্যা (int) ভাগ করলে ফলাফলও পূর্ণসংখ্যা হয়। তাই 9.0 / 5.0 বা 0.5 ব্যবহার করতে হবে।",
            codeExample = "area = 0.5 * base * height;"
        ),
        FaqItem(
            id = "faq3",
            category = "C Programming",
            questionBn = "for লুপের ভেতরের কোড শুধু একবার চলে কেন?",
            answerBn = "for(i=1; i<=5; i++); — এখানে শেষের সেমিকোলন (;) দিলে লুপটি ওখানেই শেষ হয়ে যায়! for-এর প্রথম লাইনে সেমিকোলন দেবে না।",
            codeExample = "for (i = 1; i <= 5; i++) { ... }"
        ),
        FaqItem(
            id = "faq4",
            category = "HTML",
            questionBn = "HTML টেবিলে বর্ডার দেখা যায় না কেন?",
            answerBn = "<table> ট্যাগে border=\"1\" এট্রিবিউট বা CSS border যোগ না করলে টেবিলের দাগ দেখা যায় না।",
            codeExample = "<table border=\"1\">...</table>"
        ),
        FaqItem(
            id = "faq5",
            category = "HTML",
            questionBn = "<img> ট্যাগ লেখার পরও ছবি না এলে কী করণীয়?",
            answerBn = "src এট্রিবিউটে ছবির সঠিক নাম ও এক্সটেনশন (.jpg/.png) আছে কি না দেখো এবং alt টেক্সট যোগ করো।",
            codeExample = "<img src=\"photo.jpg\" alt=\"Student\" width=\"120\">"
        ),
        FaqItem(
            id = "faq6",
            category = "App & Offline",
            questionBn = "ইন্টারনেট ছাড়া কি C ও HTML প্র্যাকটিস করা যাবে?",
            answerBn = "হ্যাঁ! সব লেসন, HTML লাইভ প্রিভিউ, অফলাইন C ইঞ্জিন, Dry-Run, পরীক্ষা ও মিস্টেক নোটবুক ১০০% অফলাইনে কাজ করে।"
        )
    )

    val sampleBatchSummaries: List<BatchStudentSummary> = listOf(
        BatchStudentSummary("তানভীর আহমেদ", "HSC-2026 (Science A)", 88, 76, "C Loops & Dry-Run", "আজ"),
        BatchStudentSummary("সুমাইয়া আক্তার", "HSC-2026 (Science A)", 94, 82, "Arrays", "আজ"),
        BatchStudentSummary("রাফিউল ইসলাম", "HSC-2026 (Science B)", 65, 48, "HTML Colspan & scanf", "গতকাল"),
        BatchStudentSummary("নুসরাত জাহান", "HSC-2026 (Science B)", 75, 62, "Switch-Case break", "২ দিন আগে")
    )
}
