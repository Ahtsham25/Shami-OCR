package com.example.ocr

import com.example.model.ExamPaper
import com.example.model.McqOption
import com.example.model.QuestionItem
import com.example.model.QuestionType

object SamplePapers {

    val urduBiologyPaper: ExamPaper = ExamPaper(
        id = "sample_urdu_bio_9th",
        title = "نہم بائیولوجی ششماہی امتحانی پرچہ (شامی اکیڈمی)",
        academyName = "شامی اکیڈمی (Shami Academy)",
        subject = "بائیولوجی (Biology)",
        gradeClass = "جماعت نہم (9th Class)",
        totalMarks = "60",
        timeAllowed = "2 گھنٹے (2 Hours)",
        language = "Urdu",
        sourceFileName = "Shami_Academy_9th_Biology_Urdu.pdf",
        rawExtractedText = """
شامی اکیڈمی (SHAMI ACADEMY)
امتحانی پرچہ: بائیولوجی (نہم جماعت) - کل نمبر: 60 - وقت: 2 گھنٹے

حصہ معروضی (کثیر الانتخابی سوالات - MCQs):
1. سیل تھیوری کس سائنسدان نے پیش کی تھی؟
   (الف) رابرٹ ہک  (ب) شوان اور شلیڈن  (ج) لیوین ہک  (د) لوئی پاسچر
2. پودوں میں خوراک کی ترسیل کے ذمہ دار ٹشوز کون سے ہیں؟
   (الف) زائلم  (ب) فلوئم  (ج) پیرنکائما  (د) کولنکائما
3. مائٹوکونڈریا کو سیل کا کیا کہا جاتا ہے؟
   (الف) باورچی خانہ  (ب) پاور ہاؤس  (ج) کنٹرول سینٹر  (د) فیکٹری
4. بائیو مالیکیولز کی کتنی بنیادی اقسام ہیں؟
   (الف) دو  (ب) تین  (ج) چار  (د) پانچ

حصہ انشائیہ - حصہ اول (مختصر سوالات):
1. بائیولوجی اور زوالوجی میں کیا فرق ہے؟
2. مائیکرو اسکوپ کی ریزولوشن اور میگنیفیکیشن کی تعریف کریں۔
3. اینزائمز کی دو اہم خصوصیات بیان کریں۔
4. پروکیریوٹک اور یوکیریوٹک سیل میں دو بنیادی فرق تحریر کریں۔
5. فوٹوسنتھیسز کا عمومی مساوات لکھیں۔
6. سیل جھلی (Cell Membrane) کا بنیادی فعل کیا ہے؟

حصہ دوم (تفصیلی / انشائیہ سوالات):
1. مائیٹوسس کے تمام مراحل (پروفیز، میٹافیز، اینافیز، ٹیلوفیز) کی تفصیلی وضاحت مع اشکال کریں۔
2. پودوں میں پانی کی ترسیل کے میکانزم کی وضاحت کریں اور ٹرانسپائریشن پل کا کردار بیان کریں۔
3. ایروبک اور این ایروبک ریسپائریشن کا تفصیلی موازنہ کریں اور اے ٹی پی کی پیداوار واضح کریں۔
        """.trimIndent(),
        questions = listOf(
            QuestionItem(
                number = 1,
                type = QuestionType.MCQ,
                statement = "سیل تھیوری کس سائنسدان نے پیش کی تھی؟",
                options = listOf(
                    McqOption("الف", "رابرٹ ہک"),
                    McqOption("ب", "شوان اور شلیڈن"),
                    McqOption("ج", "لیوین ہک"),
                    McqOption("د", "لوئی پاسچر")
                ),
                correctAnswer = "ب",
                marks = 1
            ),
            QuestionItem(
                number = 2,
                type = QuestionType.MCQ,
                statement = "پودوں میں خوراک کی ترسیل کے ذمہ دار ٹشوز کون سے ہیں؟",
                options = listOf(
                    McqOption("الف", "زائلم"),
                    McqOption("ب", "فلوئم"),
                    McqOption("ج", "پیرنکائما"),
                    McqOption("د", "کولنکائما")
                ),
                correctAnswer = "ب",
                marks = 1
            ),
            QuestionItem(
                number = 3,
                type = QuestionType.MCQ,
                statement = "مائٹوکونڈریا کو سیل کا کیا کہا جاتا ہے؟",
                options = listOf(
                    McqOption("الف", "باورچی خانہ"),
                    McqOption("ب", "پاور ہاؤس"),
                    McqOption("ج", "کنٹرول سینٹر"),
                    McqOption("د", "فیکٹری")
                ),
                correctAnswer = "ب",
                marks = 1
            ),
            QuestionItem(
                number = 4,
                type = QuestionType.MCQ,
                statement = "بائیو مالیکیولز کی کتنی بنیادی اقسام ہیں؟",
                options = listOf(
                    McqOption("الف", "دو"),
                    McqOption("ب", "تین"),
                    McqOption("ج", "چار"),
                    McqOption("د", "پانچ")
                ),
                correctAnswer = "ج",
                marks = 1
            ),
            QuestionItem(
                number = 1,
                type = QuestionType.SHORT_QUESTION,
                statement = "بائیولوجی اور زوالوجی میں کیا فرق ہے؟",
                marks = 2
            ),
            QuestionItem(
                number = 2,
                type = QuestionType.SHORT_QUESTION,
                statement = "مائیکرو اسکوپ کی ریزولوشن اور میگنیفیکیشن کی تعریف کریں۔",
                marks = 2
            ),
            QuestionItem(
                number = 3,
                type = QuestionType.SHORT_QUESTION,
                statement = "اینزائمز کی دو اہم خصوصیات بیان کریں۔",
                marks = 2
            ),
            QuestionItem(
                number = 4,
                type = QuestionType.SHORT_QUESTION,
                statement = "پروکیریوٹک اور یوکیریوٹک سیل میں دو بنیادی فرق تحریر کریں۔",
                marks = 2
            ),
            QuestionItem(
                number = 5,
                type = QuestionType.SHORT_QUESTION,
                statement = "فوٹوسنتھیسز کا عمومی مساوات لکھیں۔",
                marks = 2
            ),
            QuestionItem(
                number = 6,
                type = QuestionType.SHORT_QUESTION,
                statement = "سیل جھلی (Cell Membrane) کا بنیادی فعل کیا ہے؟",
                marks = 2
            ),
            QuestionItem(
                number = 1,
                type = QuestionType.LONG_QUESTION,
                statement = "مائیٹوسس کے تمام مراحل (پروفیز، میٹافیز، اینافیز، ٹیلوفیز) کی تفصیلی وضاحت مع اشکال کریں۔",
                marks = 8
            ),
            QuestionItem(
                number = 2,
                type = QuestionType.LONG_QUESTION,
                statement = "پودوں میں پانی کی ترسیل کے میکانزم کی وضاحت کریں اور ٹرانسپائریشن پل کا کردار بیان کریں۔",
                marks = 8
            ),
            QuestionItem(
                number = 3,
                type = QuestionType.LONG_QUESTION,
                statement = "ایروبک اور این ایروبک ریسپائریشن کا تفصیلی موازنہ کریں اور اے ٹی پی کی پیداوار واضح کریں۔",
                marks = 8
            )
        )
    )

    val englishPhysicsPaper: ExamPaper = ExamPaper(
        id = "sample_english_physics_10th",
        title = "10th Physics Term Test (Shami Academy)",
        academyName = "Shami Academy (شامی اکیڈمی)",
        subject = "Physics",
        gradeClass = "Class 10th",
        totalMarks = "60",
        timeAllowed = "2 Hours",
        language = "English",
        sourceFileName = "Shami_Academy_10th_Physics_English.pdf",
        rawExtractedText = """
SHAMI ACADEMY (شامی اکیڈمی)
Class: 10th | Subject: Physics | Total Marks: 60 | Time: 2 Hours

SECTION - A: MULTIPLE CHOICE QUESTIONS (MCQs) [12 Marks]
1. Which of the following is an example of Simple Harmonic Motion (SHM)?
   (A) Motion of a ceiling fan  (B) Motion of simple pendulum  (C) Spinning of earth  (D) Bullet fired from gun
2. The SI unit of electric potential difference is:
   (A) Ampere  (B) Ohm  (C) Volt  (D) Coulomb
3. Which electromagnetic wave has the shortest wavelength?
   (A) Radio waves  (B) Microwaves  (C) Visible light  (D) Gamma rays
4. Snells law of refraction is represented as:
   (A) n = sin(r) / sin(i)  (B) n = sin(i) / sin(r)  (C) n = cos(i) / cos(r)  (D) n = tan(i)

SECTION - B: SHORT QUESTIONS [30 Marks]
1. Define Simple Harmonic Motion and state its two main conditions.
2. What is the difference between longitudinal and transverse waves? Give one example of each.
3. State Ohm's law and write its mathematical formula with units.
4. Explain Total Internal Reflection and define the term critical angle.
5. Why are transformers used in electric power transmission networks?
6. Calculate the resistance of a 100W electric bulb connected across a 220V supply.

SECTION - C: DETAILED / LONG QUESTIONS [18 Marks]
1. Derive the formula for the time period of a simple pendulum: T = 2π√(L/g). Also explain why the mass of the bob does not affect the period.
2. State Coulomb's Law of electrostatics. Write its mathematical form in free space and explain how dielectric medium changes the electrostatic force.
3. What is electromagnetic induction? State Faraday's law and Lenz's law with practical circuit illustrations.
        """.trimIndent(),
        questions = listOf(
            QuestionItem(
                number = 1,
                type = QuestionType.MCQ,
                statement = "Which of the following is an example of Simple Harmonic Motion (SHM)?",
                options = listOf(
                    McqOption("A", "Motion of a ceiling fan"),
                    McqOption("B", "Motion of simple pendulum"),
                    McqOption("C", "Spinning of earth"),
                    McqOption("D", "Bullet fired from gun")
                ),
                correctAnswer = "B",
                marks = 1
            ),
            QuestionItem(
                number = 2,
                type = QuestionType.MCQ,
                statement = "The SI unit of electric potential difference is:",
                options = listOf(
                    McqOption("A", "Ampere"),
                    McqOption("B", "Ohm"),
                    McqOption("C", "Volt"),
                    McqOption("D", "Coulomb")
                ),
                correctAnswer = "C",
                marks = 1
            ),
            QuestionItem(
                number = 3,
                type = QuestionType.MCQ,
                statement = "Which electromagnetic wave has the shortest wavelength?",
                options = listOf(
                    McqOption("A", "Radio waves"),
                    McqOption("B", "Microwaves"),
                    McqOption("C", "Visible light"),
                    McqOption("D", "Gamma rays")
                ),
                correctAnswer = "D",
                marks = 1
            ),
            QuestionItem(
                number = 4,
                type = QuestionType.MCQ,
                statement = "Snell's law of refraction is represented as:",
                options = listOf(
                    McqOption("A", "n = sin(r) / sin(i)"),
                    McqOption("B", "n = sin(i) / sin(r)"),
                    McqOption("C", "n = cos(i) / cos(r)"),
                    McqOption("D", "n = tan(i)")
                ),
                correctAnswer = "B",
                marks = 1
            ),
            QuestionItem(
                number = 1,
                type = QuestionType.SHORT_QUESTION,
                statement = "Define Simple Harmonic Motion and state its two main conditions.",
                marks = 2
            ),
            QuestionItem(
                number = 2,
                type = QuestionType.SHORT_QUESTION,
                statement = "What is the difference between longitudinal and transverse waves? Give one example of each.",
                marks = 2
            ),
            QuestionItem(
                number = 3,
                type = QuestionType.SHORT_QUESTION,
                statement = "State Ohm's law and write its mathematical formula with units.",
                marks = 2
            ),
            QuestionItem(
                number = 4,
                type = QuestionType.SHORT_QUESTION,
                statement = "Explain Total Internal Reflection and define the term critical angle.",
                marks = 2
            ),
            QuestionItem(
                number = 5,
                type = QuestionType.SHORT_QUESTION,
                statement = "Why are transformers used in electric power transmission networks?",
                marks = 2
            ),
            QuestionItem(
                number = 6,
                type = QuestionType.SHORT_QUESTION,
                statement = "Calculate the resistance of a 100W electric bulb connected across a 220V supply.",
                marks = 2
            ),
            QuestionItem(
                number = 1,
                type = QuestionType.LONG_QUESTION,
                statement = "Derive the formula for the time period of a simple pendulum: T = 2π√(L/g). Also explain why the mass of the bob does not affect the period.",
                marks = 6
            ),
            QuestionItem(
                number = 2,
                type = QuestionType.LONG_QUESTION,
                statement = "State Coulomb's Law of electrostatics. Write its mathematical form in free space and explain how dielectric medium changes the electrostatic force.",
                marks = 6
            ),
            QuestionItem(
                number = 3,
                type = QuestionType.LONG_QUESTION,
                statement = "What is electromagnetic induction? State Faraday's law and Lenz's law with practical circuit illustrations.",
                marks = 6
            )
        )
    )

    val urduPakStudiesPaper: ExamPaper = ExamPaper(
        id = "sample_urdu_pakstudies_10th",
        title = "دہم مطالعہ پاکستان امتحانی ٹیسٹ (شامی اکیڈمی)",
        academyName = "شامی اکیڈمی (Shami Academy)",
        subject = "مطالعہ پاکستان (Pakistan Studies)",
        gradeClass = "جماعت دہم (10th Class)",
        totalMarks = "50",
        timeAllowed = "1.5 گھنٹے",
        language = "Urdu",
        sourceFileName = "Shami_Academy_PakStudies_Urdu.pdf",
        rawExtractedText = """
شامی اکیڈمی (SHAMI ACADEMY)
پیپر: مطالعہ پاکستان - جماعت: دہم - کل نمبر: 50

حصہ کثیر الانتخابی سوالات (MCQs):
1. قرارداد مقاصد کب منظور ہوئی؟
   (الف) مارچ 1940ء  (ب) اگست 1947ء  (ج) مارچ 1949ء  (د) مارچ 1956ء
2. پاکستان کا پہلا ایٹمی دھماکہ کس مقام پر کیا گیا؟
   (الف) تھر  (ب) چاغی  (ج) سیالکوٹ  (د) خضدار
3. نہری پانی کا تنازع حل کرنے کے لیے سندھ طاس معاہدہ کس سال ہوا؟
   (الف) 1958ء  (ب) 1960ء  (ج) 1965ء  (د) 1971ء

مختصر سوالات:
1. نظریہ پاکستان کی تعریف دو سطور میں بیان کریں۔
2. 1973ء کے آئین کی دو اسلامی دفعات تحریر کریں۔
3. پاکستان کی خارجہ پالیسی کے دو بنیادی مقاصد لکھیں۔
4. شاہراہ ریشم (قراقرم ہائی وے) کن دو ممالک کو آپس میں ملاتی ہے؟

تفصیلی سوالات:
1. تحریک علی گڑھ کے تعلیمی اور سیاسی خدمات پر جامع روشنی ڈالیں۔
2. پاکستان کی زراعت کو درپیش مسائل اور ان کا ممکنہ حل تفصیل سے تحریر کریں۔
        """.trimIndent(),
        questions = listOf(
            QuestionItem(
                number = 1,
                type = QuestionType.MCQ,
                statement = "قرارداد مقاصد کب منظور ہوئی؟",
                options = listOf(
                    McqOption("الف", "مارچ 1940ء"),
                    McqOption("ب", "اگست 1947ء"),
                    McqOption("ج", "مارچ 1949ء"),
                    McqOption("د", "مارچ 1956ء")
                ),
                correctAnswer = "ج",
                marks = 1
            ),
            QuestionItem(
                number = 2,
                type = QuestionType.MCQ,
                statement = "پاکستان کا پہلا ایٹمی دھماکہ کس مقام پر کیا گیا؟",
                options = listOf(
                    McqOption("الف", "تھر"),
                    McqOption("ب", "چاغی"),
                    McqOption("ج", "سیالکوٹ"),
                    McqOption("د", "خضدار")
                ),
                correctAnswer = "ب",
                marks = 1
            ),
            QuestionItem(
                number = 3,
                type = QuestionType.MCQ,
                statement = "نہری پانی کا تنازع حل کرنے کے لیے سندھ طاس معاہدہ کس سال ہوا؟",
                options = listOf(
                    McqOption("الف", "1958ء"),
                    McqOption("ب", "1960ء"),
                    McqOption("ج", "1965ء"),
                    McqOption("د", "1971ء")
                ),
                correctAnswer = "ب",
                marks = 1
            ),
            QuestionItem(
                number = 1,
                type = QuestionType.SHORT_QUESTION,
                statement = "نظریہ پاکستان کی تعریف دو سطور میں بیان کریں۔",
                marks = 2
            ),
            QuestionItem(
                number = 2,
                type = QuestionType.SHORT_QUESTION,
                statement = "1973ء کے آئین کی دو اسلامی دفعات تحریر کریں۔",
                marks = 2
            ),
            QuestionItem(
                number = 3,
                type = QuestionType.SHORT_QUESTION,
                statement = "پاکستان کی خارجہ پالیسی کے دو بنیادی مقاصد لکھیں۔",
                marks = 2
            ),
            QuestionItem(
                number = 4,
                type = QuestionType.SHORT_QUESTION,
                statement = "شاہراہ ریشم (قراقرم ہائی وے) کن دو ممالک کو آپس میں ملاتی ہے؟",
                marks = 2
            ),
            QuestionItem(
                number = 1,
                type = QuestionType.LONG_QUESTION,
                statement = "تحریک علی گڑھ کے تعلیمی اور سیاسی خدمات پر جامع روشنی ڈالیں۔",
                marks = 8
            ),
            QuestionItem(
                number = 2,
                type = QuestionType.LONG_QUESTION,
                statement = "پاکستان کی زراعت کو درپیش مسائل اور ان کا ممکنہ حل تفصیل سے تحریر کریں۔",
                marks = 8
            )
        )
    )

    val list: List<ExamPaper> = listOf(urduBiologyPaper, englishPhysicsPaper, urduPakStudiesPaper)
}
