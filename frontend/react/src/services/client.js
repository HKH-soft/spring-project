import axios from 'axios';

export const getCustomers = async() => {
    try{
        return await axios.get(`${import.meta.env.VITE_API_BASE_URL}/v1/customers`)
    }catch(e){
        throw e;
    }
}

export const getCustomer = async(customerId) => {
    try{
        return await axios.get(`${import.meta.env.VITE_API_BASE_URL}/v1/customers/${customerId}`)
    }catch(e){
        throw e;
    }
}

export const saveCustomer = async(customer) => {
    try{
        return await axios.post(`${import.meta.env.VITE_API_BASE_URL}/v1/customers`, customer)
    }catch(e){
        throw e;
    }
}

export const deleteCustomer = async(customerId) => {
    try{
        return await axios.delete(`${import.meta.env.VITE_API_BASE_URL}/v1/customers/${customerId}`)
    }catch(e){
        throw e;
    }
}

export const updateCustomer = async(customer) => {
    try{
        return await axios.put(`${import.meta.env.VITE_API_BASE_URL}/v1/customers/${customer.Id}`,customer)
    }catch(e){
        throw e;
    }
}