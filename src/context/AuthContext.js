// src/context/AuthContext.js
import React, { createContext, useState, useEffect, useContext } from 'react';
import { loginUserApi } from '../services/apiService'; // Use the renamed API function

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [currentUser, setCurrentUser] = useState(null); // Will store { username, token }
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const storedUser = JSON.parse(localStorage.getItem('user'));
        if (storedUser && storedUser.token) {
            setCurrentUser(storedUser);
        }
        setLoading(false);
    }, []);

    const login = async (credentials) => {
        try {
            const userData = await loginUserApi(credentials); // userData is { token, username }
            setCurrentUser(userData); 
            // localStorage is already handled by loginUserApi
            return userData;
        } catch (error) {
            console.error("Login failed in AuthContext:", error);
            logout();
            throw error;
        }
    };

    const logout = () => {
        localStorage.removeItem('user');
        setCurrentUser(null);
    };

    const value = {
        currentUser,
        isAuthenticated: !!currentUser?.token, // Check for token within currentUser
        login,
        logout,
        loading
    };

    return (
        <AuthContext.Provider value={value}>
            {!loading && children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    return useContext(AuthContext);
};