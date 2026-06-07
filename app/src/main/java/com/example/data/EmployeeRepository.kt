package com.example.data

import kotlinx.coroutines.flow.Flow

class EmployeeRepository(private val employeeDao: EmployeeDao) {
    val allEmployees: Flow<List<Employee>> = employeeDao.getAllEmployees()

    fun getEmployeeById(id: Int): Flow<Employee?> {
        return employeeDao.getEmployeeById(id)
    }

    suspend fun insert(employee: Employee) {
        employeeDao.insertEmployee(employee)
    }

    suspend fun update(employee: Employee) {
        employeeDao.updateEmployee(employee)
    }

    suspend fun delete(employee: Employee) {
        employeeDao.deleteEmployee(employee)
    }
}
