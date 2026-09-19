import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(localStorage.getItem('token'));
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedUser = localStorage.getItem('user');
    const savedToken = localStorage.getItem('token');
    if (savedUser && savedToken) {
      try {
        setUser(JSON.parse(savedUser));
        setToken(savedToken);
      } catch (e) {
        localStorage.clear();
      }
    }
    setLoading(false);
  }, []);

  const login = (authData) => {
    const userData = {
      id: authData.id,
      name: authData.name,
      email: authData.email,
      role: authData.role,
      hotelId: authData.hotelId
    };
    setUser(userData);
    setToken(authData.token);
    localStorage.setItem('user', JSON.stringify(userData));
    localStorage.setItem('token', authData.token);
  };

  const logout = () => {
    setUser(null);
    setToken(null);
    localStorage.removeItem('user');
    localStorage.removeItem('token');
  };

  const isOwner = user?.role === 'OWNER';
  const isManager = user?.role === 'MANAGER' || user?.role === 'OWNER';
  const isCustomer = user?.role === 'CUSTOMER';

  return (
    <AuthContext.Provider value={{ user, token, login, logout, loading, isOwner, isManager, isCustomer }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
