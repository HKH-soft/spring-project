package com.hossein.spring_project.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

// import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
// import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.hossein.spring_project.exception.DuplicateResourceExeption;
import com.hossein.spring_project.exception.RequestValidationExeption;
import com.hossein.spring_project.exception.ResourceNotFoundExeption;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
    

    @Mock
    private CustomerDAO customerDAO;
    @Mock
    private PasswordEncoder passwordEncoder;
    private final CustomerDTOMapper customerDTOMapper = new CustomerDTOMapper();
    private CustomerService underTest;
    // private AutoCloseable autoCloseableMock;
    
    @BeforeEach
    void setUp(){
        // autoCloseableMock = MockitoAnnotations.openMocks(this);
        underTest = new CustomerService(customerDAO,passwordEncoder,customerDTOMapper);
    }

    // @AfterEach
    // void tearDown() throws Exception{
    //     autoCloseableMock.close();
    // }


    @Test
    void testAddCustomer() {

        String email = "email@example.com";

        when(customerDAO.existsPersonWithEmail(email)).thenReturn(false);

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "hossein",
            email,
            "password",
            19,
            true
        );

        String passwrodHash = "!@%@456489$654654^4654%64&64@6$6@6$6&";
        when(passwordEncoder.encode(request.password())).thenReturn(passwrodHash);
        underTest.addCustomer(request);

        ArgumentCaptor<Customer> customerArgumentCaptor = ArgumentCaptor.forClass(
            Customer.class
        );
        verify(customerDAO).createCustomer(customerArgumentCaptor.capture());

        Customer customer = customerArgumentCaptor.getValue();

        assertThat(customer.getId()).isNull();
        assertThat(customer.getName()).isEqualTo(request.name());
        assertThat(customer.getEmail()).isEqualTo(request.email());
        assertThat(customer.getAge()).isEqualTo(request.age());
        assertThat(customer.getGender()).isEqualTo(request.gender());
        assertThat(customer.getPassword()).isEqualTo(passwrodHash);
        
        
    }

    @Test
    void willThrowWhenEmailExistsAddCustomer(){

        String email = "email@example.com";

        CustomerRegistrationRequest request = new CustomerRegistrationRequest(
            "hossein",
            email,
                "password", 19,
            true
        );

        when(customerDAO.existsPersonWithEmail(email)).thenReturn(true);

        assertThatThrownBy(() -> underTest.addCustomer(request))
            .isInstanceOf(DuplicateResourceExeption.class)
            .hasMessage("email already exists.");

        verify(customerDAO, never()).createCustomer(any());

        
    }

    @Test
    void testDeleteCustomer() {

        int id = 1;

        when(customerDAO.existsPersonWithId(id)).thenReturn(true);

        underTest.deleteCustomer(id);

        verify(customerDAO).removeCustomer(id);

    }

    @Test
    void willThrowWhenCustomerDoesNotExistsDeleteCustomer() {

        int id = 1;

        when(customerDAO.existsPersonWithId(id)).thenReturn(false);

        assertThatThrownBy(() -> underTest.deleteCustomer(id))
            .isInstanceOf(ResourceNotFoundExeption.class)
            .hasMessage("customer with the id: [%s] was not found!".formatted(id));
        verify(customerDAO,never()).removeCustomer(any());

    }

    @Test
    void testGetAllCustomers() {

        underTest.getAllCustomers();

        verify(customerDAO).selectAllCustomers();

    }

    @Test
    void canGetCustomerById() {

        int id = 1;
        Customer customer = new Customer(id,"hossein","email","password",19, true);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));

        CustomerDTO expected = customerDTOMapper.apply(customer);

        CustomerDTO actual = underTest.getCustomerById(id);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void throwsWhenNotFoundGetCustomerById() {

        int id = 1;
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> underTest.getCustomerById(id))
            .isInstanceOf(ResourceNotFoundExeption.class)
            .hasMessage("customer with the id: [%s] was not found!".formatted(id));
    }

    @Test
    void willThrowWhenCustomerDoesNotExistsUpdateCustomer() {

        int id = 1,age = 19;
        String email = "email",name = "hossein";

        CustomerUpdateRequest update = new CustomerUpdateRequest(
            name,
            email,
            age,
            true
        );
        when(customerDAO.existsPersonWithId(id)).thenReturn(false);

        assertThatThrownBy(() -> underTest.updateCustomer(update,id))
            .isInstanceOf(ResourceNotFoundExeption.class)
            .hasMessage("customer with the id: [%s] was not found!".formatted(id));
        
        verify(customerDAO,never()).updateCustomer(any());
        

    }
    @Test
    void willNotUpdateWhenThereIsNoChangeUpdateCustomer() {

        int id = 1,age = 12;
        String email = "email@example.com" , name = "hossein";

        CustomerUpdateRequest request = new CustomerUpdateRequest(
            name,email,age,true
        );
        Customer update = new Customer(
            name,email,"password",age, true
        );

        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(update));

        assertThatThrownBy(() -> underTest.updateCustomer(request,id))
            .isInstanceOf(RequestValidationExeption.class)
            .hasMessage("there were no changes.");

        verify(customerDAO,never()).updateCustomer(any());


    }
    @Test
    void willUpdateOnlyEmailUpdateCustomer() {

        int id = 1,age = 12;
        String email = "email@example.com" ,newEmail = "newEmail@example.com", name = "hossein";

        CustomerUpdateRequest request = new CustomerUpdateRequest(
            name,newEmail,age,true
        );
        Customer update = new Customer(
            id,name,email,"password",age, true
            );
            
        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(false);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(update));

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(
            Customer.class
        );
        
        underTest.updateCustomer(request,id);

        verify(customerDAO).updateCustomer(argumentCaptor.capture());

        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isEqualTo(id);
        assertThat(capturedCustomer.getName()).isEqualTo(name);
        assertThat(capturedCustomer.getEmail()).isEqualTo(newEmail);
        assertThat(capturedCustomer.getAge()).isEqualTo(age);
        assertThat(capturedCustomer.getGender()).isEqualTo(true);



    }
    @Test
    void willThrowWhenEmailIsAlreadyTakenUpdateCustomer() {

        int id = 1,age = 12;
        String email = "email@example.com" ,newEmail = "newEmail@example.com", name = "hossein";

        CustomerUpdateRequest request = new CustomerUpdateRequest(
            name,newEmail,age,true
        );
        Customer update = new Customer(
            name,email,"password",age, true
        );

        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(true);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(update));

        assertThatThrownBy(() -> underTest.updateCustomer(request,id))
            .isInstanceOf(DuplicateResourceExeption.class)
            .hasMessage("email already exists.");

        verify(customerDAO,never()).updateCustomer(any());


    }
    @Test
    void willUpdateOnlyNameUpdateCustomer() {

        int id = 1,age = 12;
        String email = "email@example.com", name = "hossein" , newName = "hosi";

        CustomerUpdateRequest request = new CustomerUpdateRequest(
            newName,email,age,true
        );
        Customer update = new Customer(
            id,name,email,"password",age, true
        );

        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(update));

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(
            Customer.class
        );

        underTest.updateCustomer(request,id);

        verify(customerDAO).updateCustomer(argumentCaptor.capture());

        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isEqualTo(id);
        assertThat(capturedCustomer.getName()).isEqualTo(newName);
        assertThat(capturedCustomer.getEmail()).isEqualTo(email);
        assertThat(capturedCustomer.getAge()).isEqualTo(age);
        assertThat(capturedCustomer.getGender()).isEqualTo(true);



    }
    @Test
    void willUpdateOnlyAgeUpdateCustomer() {

        int id = 1,age = 12,newAge= 155;
        String email = "email@example.com", name = "hossein";

        CustomerUpdateRequest request = new CustomerUpdateRequest(
            name,email,newAge,true
        );
        Customer update = new Customer(
            id,name,email,"password",age, true
        );

        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(update));

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(
            Customer.class
        );

        underTest.updateCustomer(request,id);

        verify(customerDAO).updateCustomer(argumentCaptor.capture());

        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isEqualTo(id);
        assertThat(capturedCustomer.getName()).isEqualTo(name);
        assertThat(capturedCustomer.getEmail()).isEqualTo(email);
        assertThat(capturedCustomer.getAge()).isEqualTo(newAge);
        assertThat(capturedCustomer.getGender()).isEqualTo(true);



    }
    @Test
    void willUpdateOnlyGenderUpdateCustomer() {

        int id = 1,age = 12;
        String email = "email@example.com", name = "hossein";

        CustomerUpdateRequest request = new CustomerUpdateRequest(
            name,email,age,false
        );
        Customer update = new Customer(
            id,name,email,"password",age, true
        );

        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(update));

        ArgumentCaptor<Customer> argumentCaptor = ArgumentCaptor.forClass(
            Customer.class
        );

        underTest.updateCustomer(request,id);

        verify(customerDAO).updateCustomer(argumentCaptor.capture());

        Customer capturedCustomer = argumentCaptor.getValue();

        assertThat(capturedCustomer.getId()).isEqualTo(id);
        assertThat(capturedCustomer.getName()).isEqualTo(name);
        assertThat(capturedCustomer.getEmail()).isEqualTo(email);
        assertThat(capturedCustomer.getAge()).isEqualTo(age);
        assertThat(capturedCustomer.getGender()).isEqualTo(false);



    }

    @Test
    void CanUpdateAllCustomersProperites(){
        
        int id = 1;

        Customer customer = new Customer(
            id,
            "name",
            "email",
            "password",
            21, true
        );

        String newEmail = "newEmail";
        when(customerDAO.existsPersonWithId(id)).thenReturn(true);
        when(customerDAO.selectCustomerById(id)).thenReturn(Optional.of(customer));
        when(customerDAO.existsPersonWithEmail(newEmail)).thenReturn(false);
        CustomerUpdateRequest request = new CustomerUpdateRequest(
            "newName",
            newEmail,
            123,
            false
        );

        underTest.updateCustomer(request,id);

        ArgumentCaptor<Customer> captor = ArgumentCaptor.forClass(
            Customer.class
        );
        verify(customerDAO).updateCustomer(captor.capture());
        Customer actual = captor.getValue();
        assertThat(actual.getId()).isEqualTo(id);
        assertThat(actual.getName()).isEqualTo("newName");
        assertThat(actual.getEmail()).isEqualTo(newEmail);
        assertThat(actual.getAge()).isEqualTo(123);
        assertThat(actual.getGender()).isEqualTo(false);
    }
    
}
