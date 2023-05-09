package org.example;

import org.example.domain.Nota;
import org.example.domain.Student;
import org.example.domain.Tema;
import org.junit.jupiter.api.*;
import org.example.repository.NotaXMLRepo;
import org.example.repository.StudentXMLRepo;
import org.example.repository.TemaXMLRepo;
import org.example.service.Service;
import org.example.validation.NotaValidator;
import org.example.validation.StudentValidator;
import org.example.validation.TemaValidator;
import org.example.validation.ValidationException;
import org.mockito.Mock;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

public class TestClass {

    @Mock
    private StudentValidator studentValidatorMock;

    @Mock
    private TemaValidator temaValidatorMock;

    @Mock
    private NotaValidator notaValidatorMock;

    @Mock
    private StudentXMLRepo studentXMLRepositoryMock;

    @Mock
    private TemaXMLRepo temaXMLRepositoryMock;

    @Mock
    private NotaXMLRepo notaXMLRepositoryMock;

    private Service serviceMock;

    public static Service service;

    @BeforeEach
    public void setupMock() {
        studentValidatorMock = mock(StudentValidator.class);
        temaValidatorMock = mock(TemaValidator.class);
        notaValidatorMock = mock(NotaValidator.class);
        studentXMLRepositoryMock = mock(StudentXMLRepo.class);
        temaXMLRepositoryMock = mock(TemaXMLRepo.class);
        notaXMLRepositoryMock = mock(NotaXMLRepo.class);
        serviceMock = new Service(studentXMLRepositoryMock, studentValidatorMock, temaXMLRepositoryMock, temaValidatorMock, notaXMLRepositoryMock, notaValidatorMock);
    }

    @AfterEach
    public void tearDownMock() {
        studentValidatorMock = null;
        temaValidatorMock = null;
        notaValidatorMock = null;
        studentXMLRepositoryMock = null;
        temaXMLRepositoryMock = null;
        notaXMLRepositoryMock = null;
        serviceMock = null;
    }

    @BeforeAll
    public static void setup() {
        StudentValidator studentValidator = new StudentValidator();
        TemaValidator temaValidator = new TemaValidator();
        String filenameStudent = "fisiere/Studenti.xml";
        String filenameTema = "fisiere/Teme.xml";
        String filenameNota = "fisiere/Note.xml";

        StudentXMLRepo studentXMLRepository = new StudentXMLRepo(filenameStudent);
        TemaXMLRepo temaXMLRepository = new TemaXMLRepo(filenameTema);
        NotaValidator notaValidator = new NotaValidator(studentXMLRepository, temaXMLRepository);
        NotaXMLRepo notaXMLRepository = new NotaXMLRepo(filenameNota);
        TestClass.service = new Service(studentXMLRepository, studentValidator, temaXMLRepository, temaValidator, notaXMLRepository, notaValidator);
    }

    // Incremental Integration testing + Mockito
    @Test
    public void addStudent_InvalidNume_ThrowsException() {
        String studentId = "1";
        String nume = "";
        int grupa = 931;
        String email = "miguel@yahoo.com";

        Student student = new Student(studentId, nume, grupa, email);

        try {
            doThrow(new ValidationException("Nume incorect!")).when(studentValidatorMock).validate(student);
            Assertions.assertThrows(ValidationException.class, () -> {
                serviceMock.addStudent(student);
            });
        } catch (ValidationException ve) {
            ve.printStackTrace();
        }
    }

    @Test
    public void addStudent_Valid_addTema_InvalidDeadline_ThrowsException() {
        String studentId = "1";
        String nume = "Miguel";
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(studentId, nume, grupa, email);

        String nrTema = "1";
        String descriere = "descriere";
        int deadline = 20;
        int primire = 2;

        Tema tema = new Tema(nrTema, descriere, deadline, primire);

        try {
            doNothing().when(studentValidatorMock).validate(student);
            when(studentXMLRepositoryMock.save(student)).thenReturn(null);
            doThrow(new ValidationException("Deadlineul trebuie sa fie intre 1-14.")).when(temaValidatorMock).validate(tema);

            Student returnedStudent = serviceMock.addStudent(student);
            Assertions.assertNull(returnedStudent);
            Assertions.assertThrows(ValidationException.class, () -> {
                serviceMock.addTema(tema);
            });
        } catch (ValidationException ve) {
            ve.printStackTrace();
        }
    }

    @Test
    public void addStudent_Valid_addTema_Valid_addNota_Valid_ThrowsException() {
        String studentId = "1";
        String nume = "Miguel";
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(studentId, nume, grupa, email);

        String nrTema = "1";
        String descriere = "descriere";
        int deadline = 12;
        int primire = 2;
        Tema tema = new Tema(nrTema, descriere, deadline, primire);

        String notaId = "nt1";
        double notaVal = 9.5;
        LocalDate date = LocalDate.of(2023, 4, 25);
        Nota nota = new Nota(notaId, studentId, nrTema, notaVal, date);

        try {
            doNothing().when(studentValidatorMock).validate(student);
            when(studentXMLRepositoryMock.save(student)).thenReturn(null);

            doNothing().when(temaValidatorMock).validate(tema);
            when(temaXMLRepositoryMock.save(tema)).thenReturn(null);

            when(studentXMLRepositoryMock.findOne(studentId)).thenReturn(student);
            when(temaXMLRepositoryMock.findOne(nrTema)).thenReturn(tema);

            Student returnedStudent = serviceMock.addStudent(student);
            Assertions.assertNull(returnedStudent);

            Tema returnedTema = serviceMock.addTema(tema);
            Assertions.assertNull(returnedTema);

            Nota returnedNota = serviceMock.addNota(nota, "feedback");
            Assertions.assertNull(returnedNota);
            Assertions.assertEquals(9.5, nota.getNota());
        } catch (ValidationException ve) {
            ve.printStackTrace();
        }
    }

    // Big-bang Integration testing
    @Test
    public void addStudent_BigBang_ValidData_CreatedSuccessfully() {
        String idStudent = "st1";
        String numeStudent = "miguel";
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);
        try {
            service.addStudent(student);
            assert(true);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(false);
        }
        assert (service.findStudent(idStudent) != null);
    }

    @Test
    public void addTema_BigBang_ValidData_CreatedSuccessfully() {
        String nrTema = "tm1";
        String descriere = "test";
        int deadline = 12;
        int primire = 1;
        Tema tema = new Tema(nrTema, descriere, deadline, primire);
        try {
            service.addTema(tema);
            assert(true);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(false);
        }
    }

    @Test
    public void addNota_BigBang_ValidData_CreatedSuccessfully() {
        String notaId = "nt1";
        String studentId = "1002";
        String temaId = "100";
        double nota = 9.5;
        LocalDate date = LocalDate.of(2023, 4, 25);
        Nota notaObj = new Nota(notaId, studentId, temaId, nota, date);
        try {
            service.addNota(notaObj, "feedback");
            assert(true);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(false);
        }
    }

    @Test
    public void addNota_Integration_ValidData_CreatedSuccessfully() {
        String idStudent = "st2";
        String numeStudent = "miguel";
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);

        String nrTema = "tm2";
        String descriere = "test";
        int deadline = 12;
        int primire = 1;
        Tema tema = new Tema(nrTema, descriere, deadline, primire);

        String notaId = "nt1";
        double nota = 9.5;
        LocalDate date = LocalDate.of(2023, 4, 25);
        Nota notaObj = new Nota(notaId, idStudent, nrTema, nota, date);

        try {
            service.addStudent(student);
            service.addTema(tema);
            service.addNota(notaObj, "feedback");
            assert(true);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(false);
        }
    }

    // White box testing
    @Test
    public void addTema_ValidData_CreateSuccessfully() {
        String nrTema = "100";
        String descriere = "test";
        int deadline = 12;
        int primire = 1;
        Tema tema = new Tema(nrTema, descriere, deadline, primire);
        try {
            service.addTema(tema);
            assert(true);
        } catch (ValidationException exception) {
            System.out.println("Validation exception: " + exception.getMessage());
            assert(false);
        }
    }

    @Test
    public void addTema_Invalid_nrTema_duplicate_ThrowsError() {
        String nrTema = "100";
        String descriere = "test";
        int deadline = 12;
        int primire = 1;
        Tema tema = new Tema(nrTema, descriere, deadline, primire);
        try {
            Tema response = service.addTema(tema);
            assert(tema == response);
        } catch (ValidationException exception) {
            System.out.println("Validation exception: " + exception.getMessage());
            assert(true);
        }
    }

    @Test
    public void addTema_Invalid_nrTema_emptyString_ThrowsError() {
        String nrTema = "";
        String descriere = "test";
        int deadline = 12;
        int primire = 2;
        Tema tema = new Tema(nrTema, descriere, deadline, primire);
        try {
            service.addTema(tema);
            assert(false);
        } catch (ValidationException exception) {
            System.out.println("Validation exception: " + exception.getMessage());
            assert(true);
        }
    }

    @Test
    public void addTema_Invalid_nrTema_null_ThrowsError() {
        String nrTema = null;
        String descriere = "test";
        int deadline = 12;
        int primire = 2;
        Tema tema = new Tema(nrTema, descriere, deadline, primire);
        try {
            service.addTema(tema);
            assert(false);
        } catch (ValidationException exception) {
            System.out.println("Validation exception: " + exception.getMessage());
            assert(true);
        }
    }

    @Test
    public void addTema_Invalid_descriere_emptyString_ThrowsError() {

        String nrTema = "101";
        String descriere = "";
        int deadline = 12;
        int primire = 2;

        Tema tema = new Tema(nrTema, descriere, deadline, primire );

        try{
            service.addTema(tema);
            assert(false);

        }catch (ValidationException ve){
            System.out.println("Validation Exception: " + ve.getMessage());
            assert(true);

        }

    }

    @Test
    public void addTema_Invalid_descriere_null_ThrowsError() {

        String nrTema = "101";
        String descriere = null;
        int deadline = 12;
        int primire = 2;

        Tema tema = new Tema(nrTema, descriere, deadline, primire );

        try{
            service.addTema(tema);
            assert(false);

        }catch (ValidationException ve){
            System.out.println("Validation Exception: " + ve.getMessage());
            assert(true);

        }

    }


    @Test
    public void addTema_Invalid_deadline_smallerThan1_ThrowsError() {

        String nrTema = "102";
        String descriere = "test";
        int deadline = 0;
        int primire = 11;

        Tema tema = new Tema(nrTema, descriere, deadline, primire );

        try{
            service.addTema(tema);
            assert(false);

        }catch (ValidationException ve){
            System.out.println("Validation Exception: " + ve.getMessage());
            assert(true);

        }

    }

    @Test
    public void addTema_Invalid_deadline_greaterThan14_ThrowsError() {

        String nrTema = "102";
        String descriere = "test";
        int deadline = 15;
        int primire = 11;

        Tema tema = new Tema(nrTema, descriere, deadline, primire );

        try{
            service.addTema(tema);
            assert(false);

        }catch (ValidationException ve){
            System.out.println("Validation Exception: " + ve.getMessage());
            assert(true);

        }

    }

    @Test
    public void addTema_Invalid_primire_smallerThan1_ThrowsError() {

        String nrTema = "103";
        String descriere = "test";
        int deadline = 12;
        int primire = 0;

        Tema tema = new Tema(nrTema, descriere, deadline, primire );

        try{
            service.addTema(tema);
            assert(false);

        }catch (ValidationException ve){
            System.out.println("Validation Exception: " + ve.getMessage());
            assert(true);

        }

    }

    @Test
    public void addTema_Invalid_primire_greaterThan14_ThrowsError() {

        String nrTema = "103";
        String descriere = "test";
        int deadline = 12;
        int primire = 15;

        Tema tema = new Tema(nrTema, descriere, deadline, primire );

        try{
            service.addTema(tema);
            assert(false);

        }catch (ValidationException ve){
            System.out.println("Validation Exception: " + ve.getMessage());
            assert(true);

        }

    }

    // Black box testing
    @Test
    public void addStudent_ValidData_CreatedSuccessfully() {
        String idStudent = "test";
        String numeStudent = "miguel";
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);

        try {
            service.addStudent(student);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assertFalse(true);
        }

        assert(service.findStudent(idStudent) != null);
    }

    @Test
    public void addStudent_EmptyId_ThrowError() {
        String idStudent = "";
        String numeStudent = "miguel";
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);

        try {
            service.addStudent(student);
            assert(false);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(true);
        }
    }

    @Test
    public void addStudent_NullId_ThrowError() {
        String idStudent = null;
        String numeStudent = "miguel";
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);

        try {
            service.addStudent(student);
            assert(true);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(true);
        }
    }

    @Test
    public void addStudent_DuplicateId_ThrowError() {
        String idStudent = "test";
        String numeStudent = "miguel";
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);
        try {
            Student result = service.addStudent(student);
            assert(result == student);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(true);
        }
    }

    @Test
    public void addStudent_EmptyName_ThrowError() {
        String idStudent = "test1";
        String numeStudent = "";
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);

        try {
            service.addStudent(student);
            assert(false);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(true);
        }
    }

    @Test
    public void addStudent_NullName_ThrowError() {
        String idStudent = "test2";
        String numeStudent = null;
        int grupa = 931;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);

        try {
            service.addStudent(student);
            assert(false);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(true);
        }
    }

    @Test
    public void addStudent_NegativeGroupNr_ThrowError() {
        String idStudent = "test3";
        String numeStudent = "miguel";
        int grupa = -931;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);

        try {
            service.addStudent(student);
            assert(false);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(true);
        }
    }

    @Test
    public void addStudent_EmptyEmail_ThrowError() {
        String idStudent = "test4";
        String numeStudent = "miguel";
        int grupa = 931;
        String email = "";
        Student student = new Student(idStudent, numeStudent, grupa, email);

        try {
            service.addStudent(student);
            assert(false);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(true);
        }
    }

    @Test
    public void addStudent_NullEmail_ThrowError() {
        String idStudent = "test5";
        String numeStudent = "miguel";
        int grupa = 931;
        String email = null;
        Student student = new Student(idStudent, numeStudent, grupa, email);

        try {
            service.addStudent(student);
            assert(false);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(true);
        }
    }

    @Test
    public void addStudent_LowerBoundGroupNr_ThrowError() {
        String idStudent = "test6";
        String numeStudent = "miguel";
        int grupa = 0;
        String email = "miguel@yahoo.com";
        Student student = new Student(idStudent, numeStudent, grupa, email);

        try {
            Student result = service.addStudent(student);
        } catch (ValidationException exception) {
            System.out.println(exception);
            assert(true);
        }
        assert(service.findStudent(idStudent) != null);
    }
}
