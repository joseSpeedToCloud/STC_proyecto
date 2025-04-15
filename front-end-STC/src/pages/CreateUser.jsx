import React, { useState, useEffect } from 'react';
import Sidebar from '../components/Sidebar';
import axios from 'axios';
import { useNavigate, useLocation } from '@tanstack/react-router';

export default function CreateUser() {
  const navigate = useNavigate();
  const location = useLocation();
  const isEdit = location.state?.isEdit || false;
  const editUsername = location.search?.username || location.state?.userData?.username || '';

  const [userRole, setUserRole] = useState('');
  const [loading, setLoading] = useState(false);
  const [formData, setFormData] = useState({
    username: '',
    lastname: '',
    email: '',
    department: '',
    password: '',
    confirmPassword: '',
    globalRole: 'VIEWER'
  });
  
  const [passwordType, setPasswordType] = useState('password');
  const [confirmPasswordType, setConfirmPasswordType] = useState('password');
  const [errors, setErrors] = useState({
    username: '',
    password: '',
    backend: ''
  });
  const [originalUserRole, setOriginalUserRole] = useState('');

  const SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?";

  useEffect(() => {
    const role = localStorage.getItem('userRole');
    setUserRole(role);
    
    if (role === 'VIEWER') {
      setFormData(prev => ({ ...prev, globalRole: 'VIEWER' }));
    }
    
    if (isEdit && editUsername) {
      setLoading(true);
      axios.get(`http://localhost:8080/api/cloud/users/${editUsername}`)
        .then(response => {
          const userData = response.data;
          setFormData({
            username: userData.username,
            lastname: userData.lastname || '',
            email: userData.email || '',
            department: userData.department || '',
            password: '',
            confirmPassword: '',
            globalRole: userData.rol
          });
          setOriginalUserRole(userData.rol);
          setLoading(false);
        })
        .catch(error => {
          setErrors(prev => ({ ...prev, backend: 'Error loading user data' }));
          console.error('Error:', error);
          setLoading(false);
        });
    }
  }, [isEdit, editUsername]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const validarPassword = (password) => {
    if (isEdit && !password) return {};
    
    const errorMsgs = {
      length: password.length < 6 ? "Password must be at least 6 characters long" : "",
      maxLength: password.length > 10 ? "Password cannot be longer than 10 characters" : "",
      uppercase: !/[A-Z]/.test(password) ? "Password must contain at least one uppercase letter" : "",
      lowercase: !/[a-z]/.test(password) ? "Password must contain at least one lowercase letter" : "",
      number: !/[0-9]/.test(password) ? "Password must contain at least one number" : "",
      specialChar: !new RegExp(`[${SPECIAL_CHARACTERS.replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&')}]`).test(password) 
        ? `Password must contain at least one special character: ${SPECIAL_CHARACTERS}` 
        : ""
    };

    return errorMsgs;
  };

  const handleSubmit = async () => {
    let hasError = false;
    const newErrors = { username: '', password: '', backend: '' };

    // Validation logic
    if (!isEdit && !formData.username.trim()) {
      newErrors.username = 'Username is required';
      hasError = true;
    }

    if (!isEdit && !formData.password) {
      newErrors.password = 'Password is required';
      hasError = true;
    } else if (formData.password) {
      const passwordErrors = validarPassword(formData.password);
      const combinedPasswordErrors = Object.values(passwordErrors).filter(msg => msg).join(" ");
      
      if (combinedPasswordErrors) {
        newErrors.password = combinedPasswordErrors;
        hasError = true;
      } else if (formData.password !== formData.confirmPassword) {
        newErrors.password = 'Passwords do not match';
        hasError = true;
      }
    }

    // Role permission validation
    if (userRole === 'VIEWER') {
      if (formData.globalRole !== 'VIEWER') {
        newErrors.backend = 'Viewers can only create or edit viewer accounts';
        hasError = true;
      }
      
      if (isEdit && originalUserRole === 'ADMINISTRADOR') {
        newErrors.backend = 'Viewers cannot edit administrator accounts';
        hasError = true;
      }
      
      if (isEdit && formData.password) {
        newErrors.backend = 'Viewers cannot change passwords of other users';
        hasError = true;
      }
    }

    setErrors(newErrors);

    if (!hasError) {
      try {
        const finalRole = userRole === 'VIEWER' ? 'VIEWER' : formData.globalRole;
        const userData = {
          ...(isEdit ? {} : { username: formData.username }),
          lastname: formData.lastname,
          email: formData.email,
          department: formData.department,
          rol: finalRole
        };

        // Only include password if it's not empty and user has permission
        if (formData.password && formData.password.trim() !== '' && 
            (userRole === 'ADMINISTRADOR' || !isEdit)) {
          userData.password = formData.password;
        }

        console.log("Submitting user data:", userData);

        const endpoint = isEdit 
          ? `http://localhost:8080/api/cloud/users/${editUsername}`
          : 'http://localhost:8080/api/cloud/users';
        
        const method = isEdit ? 'put' : 'post';

        const response = await axios[method](endpoint, userData, {
          headers: { 'User-Role': userRole }
        });
        
        console.log("Server response:", response.data);
        navigate({ to: '/users' });
      } catch (error) {
        console.error("Error during submission:", error);
        const errorMessage = error.response?.data || 'An unexpected error occurred';
        setErrors(prev => ({ ...prev, backend: errorMessage }));
      }
    }
  };

  if (loading) {
    return (
      <Sidebar showBackButton={true}>
        <div className="flex justify-center items-center h-screen">
          <p>Loading user data...</p>
        </div>
      </Sidebar>
    );
  }

  return (
    <Sidebar showBackButton={true}>
      <div className="p-6">
        <h1 className="text-2xl mb-6 text-[#3F9BB9]">{isEdit ? 'Edit User' : 'Create User'}</h1>
        <div className="flex flex-col space-y-6 p-6 border-2 border-[#ccc] rounded-[5px] max-w-[900px] mx-auto">
          <h3 className="font-bold text-[#0B6A8D]">User Information</h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <input
                name="username"
                className={`w-full p-2 border-2 rounded ${errors.username ? 'border-red-500' : 'border-[#ccc]'}`}
                placeholder="Username *"
                value={formData.username}
                onChange={handleInputChange}
                disabled={isEdit}
              />
              {errors.username && <p className="text-red-500 text-sm mt-1">{errors.username}</p>}
            </div>

            <div>
              <input
                name="lastname"
                className="w-full p-2 border-2 border-[#ccc] rounded"
                placeholder="Last Name"
                value={formData.lastname}
                onChange={handleInputChange}
              />
            </div>

            <div>
              <input
                name="email"
                className="w-full p-2 border-2 border-[#ccc] rounded"
                placeholder="Email"
                value={formData.email}
                onChange={handleInputChange}
              />
            </div>

            <div>
              <input
                name="department"
                className="w-full p-2 border-2 border-[#ccc] rounded"
                placeholder="Department"
                value={formData.department}
                onChange={handleInputChange}
              />
            </div>
          </div>

          {(!isEdit || userRole === 'ADMINISTRADOR') && (
            <div>
              <h3 className="font-bold text-[#0B6A8D] mb-4">Password</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div className="relative">
                  <input
                    type={passwordType}
                    name="password"
                    className={`w-full p-2 border-2 rounded ${errors.password ? 'border-red-500' : 'border-[#ccc]'}`}
                    placeholder={isEdit ? "New Password (optional)" : "Password *"}
                    value={formData.password}
                    onChange={handleInputChange}
                  />
                  <button
                    type="button"
                    className="absolute right-2 top-1/2 -translate-y-1/2 text-sm text-[#0B6A8D]"
                    onClick={() => setPasswordType(prev => prev === 'password' ? 'text' : 'password')}
                  >
                    {passwordType === 'password' ? 'Show' : 'Hide'}
                  </button>
                </div>

                <div className="relative">
                  <input
                    type={confirmPasswordType}
                    name="confirmPassword"
                    className={`w-full p-2 border-2 rounded ${errors.password ? 'border-red-500' : 'border-[#ccc]'}`}
                    placeholder={isEdit ? "Confirm New Password" : "Confirm Password *"}
                    value={formData.confirmPassword}
                    onChange={handleInputChange}
                  />
                  <button
                    type="button"
                    className="absolute right-2 top-1/2 -translate-y-1/2 text-sm text-[#0B6A8D]"
                    onClick={() => setConfirmPasswordType(prev => prev === 'password' ? 'text' : 'password')}
                  >
                    {confirmPasswordType === 'password' ? 'Show' : 'Hide'}
                  </button>
                </div>
              </div>
              {errors.password && (
                <p className="text-red-500 text-sm mt-2">{errors.password}</p>
              )}
            </div>
          )}

          <div>
            <h3 className="font-bold text-[#0B6A8D] mb-4">Global Permissions</h3>
            <div className="space-y-3">
              <label className="flex items-center space-x-2">
                <input
                  type="radio"
                  name="globalRole"
                  value="ADMINISTRADOR"
                  checked={formData.globalRole === 'ADMINISTRADOR'}
                  onChange={handleInputChange}
                  disabled={userRole === 'VIEWER'}
                />
                <span className={userRole === 'VIEWER' ? 'text-gray-400' : ''}>
                  Administrator
                </span>
              </label>
              <label className="flex items-center space-x-2">
                <input
                  type="radio"
                  name="globalRole"
                  value="VIEWER"
                  checked={formData.globalRole === 'VIEWER'}
                  onChange={handleInputChange}
                />
                <span>Viewer</span>
              </label>
            </div>
          </div>

          {errors.backend && (
            <div className="bg-red-50 border-l-4 border-red-500 text-red-700 p-4">
              <p className="font-bold">Error:</p>
              <p>{errors.backend}</p>
            </div>
          )}

          <div className="flex justify-end space-x-4">
            <button
              className="px-4 py-2 bg-gray-200 rounded hover:bg-gray-300"
              onClick={() => navigate({ to: '/users' })}
            >
              Cancel
            </button>
            <button
              className="px-4 py-2 bg-[#3F9BB9] text-white rounded hover:bg-[#2D7A98]"
              onClick={handleSubmit}
            >
              {isEdit ? 'Save Changes' : 'Create User'}
            </button>
          </div>
        </div>
      </div>
    </Sidebar>
  );
}
