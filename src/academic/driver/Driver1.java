package academic.driver;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import academic.model.Course;
import academic.model.Enrollment;
import academic.model.Student;
import academic.model.Lecturer;

public class Driver1 {

    final static Scanner scanner = new Scanner(System.in);

    private static List<Course> courses = new ArrayList<>();
    private static List<Student> students = new ArrayList<>();
    private static List<Enrollment> enrollments = new ArrayList<>();
    private static List<Lecturer> lecturers = new ArrayList<>();
    private static List<String> inputList = new ArrayList<>();

    public static void main(String[] args) {
        // Ambil input pengguna sampai diberikan tanda ---
        do {
            String input = scanner.nextLine();
            if (input.equals("---")) {
                break;
            }
            inputList.add(input);
        } while (true);

        // Proses setiap perintah yang diberikan pengguna
        for (String input : inputList) {
            String[] command = input.split("#");

            switch (command[0]) {
                case "lecturer-add":
                    if (command.length == 6) {
                        Lecturer lecturer = new Lecturer(command[1], command[2], command[3], command[4], command[5]);
                        if (!isDuplicateLecturer(lecturer, lecturers)) {
                            lecturers.add(lecturer);
                        }
                    }
                    break;
                case "course-add":
                    if (command.length >= 5) {
                        String[] lecturerInitials = command[4].split(",");
                        List<String> lecturerNames = new ArrayList<>();
                        for (String lecturerInitial : lecturerInitials) {
                            for (Lecturer lecturer : lecturers) {
                                if (lecturer.getInitial().equals(lecturerInitial)) {
                                    lecturerNames.add(lecturer.getInitial() + " (" + lecturer.getEmail() + ")");
                                    break;
                                }
                            }
                        }
                        Course course = new Course(command[1], command[2], Integer.parseInt(command[3]), command[4], lecturerNames);
                        if (!isDuplicateCourse(course, courses)) {
                            courses.add(course);
                        }
                    }
                    break;
                case "student-add":
                    if (command.length == 5) {
                        Student student = new Student(command[1], command[2], command[3], command[4]);
                        if (!isDuplicateStudent(student, students)) {
                            students.add(student);
                        }
                    }
                    break;
                case "enrollment-add":
                    if (command.length >= 5) {
                        Enrollment enrollment = new Enrollment(command[1], command[2], command[3], command[4]);
                        if (!enrollments.contains(enrollment)) {
                            enrollments.add(enrollment);
                        }
                    }
                    break;
                case "enrollment-grade":
                    if (command.length == 6) {
                        String courseCode = command[1];
                        String studentId = command[2];
                        String academicYear = command[3];
                        String semester = command[4];
                        String grade = command[5];

                        Enrollment targetEnrollment = findEnrollment(courseCode, studentId, academicYear, semester, enrollments);

                        if (targetEnrollment != null) {
                            targetEnrollment.setGrade(grade);
                        }
                    }
                    break;
                case "student-details":
                    if (command.length == 2) {
                        String studentId = command[1];
                        Student targetStudent = findStudentById(studentId, students);
                        if (targetStudent != null) {
                            double gpa = calculateGPA(studentId, enrollments, courses);
                            int totalCredit = calculateTotalCredits(studentId, enrollments, courses);
                            System.out.println(targetStudent.getId() + "|" + targetStudent.getName() + "|" + targetStudent.getYear() + "|" + targetStudent.getStudyProgram() + "|" + String.format("%.2f", gpa) + "|" + totalCredit);
                        }
                    }
                    break;
                case "enrollment-remedial":
    for (Enrollment enr : enrollments) {
        if (enr.getCode().equals(command[1]) &&
                enr.getId().equals(command[2]) &&
                enr.getAcademicYear().equals(command[3]) &&
                enr.getSemester().equals(command[4])) {
            if (enr.getGrade().equals("None")) {
                break;
            } else {
                if (enr.getPreviousGrade().equals("None")) {
                    enr.setPreviousGrade(command[5]);
                    enr.setGrade("Remedial");
                } else {
                    enr.setGrade(enr.getGrade() + "(" + command[5] + ")");
                }
            }
        }
    }
    break;

                default:
                    break;
            }
        }

        // Print data yang sudah diinput
        for (Lecturer lecturer : lecturers) {
            System.out.println(lecturer);
        }

        for (Course course : courses) {
            System.out.println(course);

        }

        for (Student student : students) {
            System.out.println(student);
        }

        for (Enrollment enrollment : enrollments) {
            System.out.println(enrollment);
        }
    }

    private static boolean isDuplicateCourse(Course course, List<Course> courses) {
        return courses.stream().anyMatch(c -> c.getCode().equals(course.getCode()));
    }

    private static boolean isDuplicateStudent(Student student, List<Student> students) {
        return students.stream().anyMatch(s -> s.getId().equals(student.getId()));
    }

    private static boolean isDuplicateLecturer(Lecturer lecturer, List<Lecturer> lecturers) {
        return lecturers.stream().anyMatch(e -> e.getName().equals(lecturer.getName()));
    }

    private static Enrollment findEnrollment(String courseCode, String studentId, String academicYear, String semester, List<Enrollment> enrollments) {
        for (Enrollment enrollment : enrollments) {
            if (enrollment.getCode().equals(courseCode) &&
                    enrollment.getId().equals(studentId) &&
                    enrollment.getAcademicYear().equals(academicYear) &&
                    enrollment.getSemester().equals(semester)) {
                return enrollment;
            }
        }
        return null;
    }

   private static double calculateGPA(String studentId, List<Enrollment> enrollments, List<Course> courses) {
    Map<String, String> latestGrades = new HashMap<>();

    // Mengumpulkan nilai terakhir untuk setiap mata kuliah yang diulang
    for (Enrollment enrollment : enrollments) {
        if (enrollment.getId().equals(studentId) && enrollment.getGrade() != null && !enrollment.getGrade().equals("None")) {
            latestGrades.put(enrollment.getCode(), enrollment.getGrade());
        }
    }

    double totalScore = 0;
    int totalCredits = 0;

    // Menghitung total skor dan total kredit menggunakan nilai terakhir untuk setiap mata kuliah
    for (String courseCode : latestGrades.keySet()) {
        String latestGrade = latestGrades.get(courseCode);
        for (Course course : courses) {
            if (course.getCode().equals(courseCode)) {
                totalScore += convertGradeToScore(latestGrade) * course.getCredit();
                totalCredits += course.getCredit();
                break;
            }
        }
    }

    if (totalCredits == 0) {
        return 0;
    }

    return totalScore / totalCredits;
}


private static int calculateTotalCredits(String studentId, List<Enrollment> enrollments, List<Course> courses) {    Set<String> enrolledCourses = new HashSet<>();
    int totalCredits = 0;

    // Mengumpulkan semua mata kuliah yang diambil oleh mahasiswa
    for (Enrollment enrollment : enrollments) {
        if (enrollment.getId().equals(studentId)) {
            enrolledCourses.add(enrollment.getCode());
        }
    }

    // Menghitung total kredit hanya untuk setiap mata kuliah yang diambil, tidak termasuk kursus yang diulang
    for (String courseCode : enrolledCourses) {
        for (Course course : courses) {
            if (course.getCode().equals(courseCode)) {
                totalCredits += course.getCredit();
                break;
            }
        }
    }

    return totalCredits;
}


    private static double convertGradeToScore (String grade) {
        if (grade.equals("A")) {
            return 4.0;
        } else if (grade.equals("AB")) {
            return 3.5;
        } else if (grade.equals("B")) {
            return 3.0;
        } else if (grade.equals("BC")) {
            return 2.5;
        } else if (grade.equals("C")) {
            return 2.0;
        } else if (grade.equals("D")) {
            return 1.0;
        } else if (grade.equals("E")) {
            return 0.0;
        } else {
            return 0.0;
        }
    }

    private static int getCourseCredit(String courseCode) {
        for (Course course : courses) {
            if (course.getCode().equals(courseCode)) {
                return course.getCredit();
            }
        }
        return 0;
    }

    private static Student findStudentById(String studentId, List<Student> students) {
        for (Student student : students) {
            if (student.getId().equals(studentId)) {
                return student;
            }
        }
        return null;
    }
}






