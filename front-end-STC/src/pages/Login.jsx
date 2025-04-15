import React, { useState, useEffect } from "react";
import axios from "axios";
import logo from "../assets/logo.png";
import { useNavigate } from "@tanstack/react-router";

const Login = () => {
  const [user, setUser] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [errorMessages, setErrorMessages] = useState({
    user: "",
    password: ""
  });
  const [noUsers, setNoUsers] = useState(null);

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [isChangingPassword, setIsChangingPassword] = useState(false);
  const [formSubmitted, setFormSubmitted] = useState(false);

  const navigate = useNavigate();

  const SPECIAL_CHARACTERS = "!@#$%^&*()_+-=[]{};':\"\\|,.<>/?";

  useEffect(() => {
    (async () => {
      try {
        const response = await axios.get("http://localhost:8080/api/cloud/users");
        if (response.status === 204) {
          setNoUsers(true);
          setUser("admin_root");
          setPassword("Stc20@");
        } else {
          setNoUsers(false);
          setUser("");
        }
      } catch (error) {
        console.error("Error al verificar usuarios:", error);
        setNoUsers(false);
      }
    })();
  }, []);

  const validarPassword = (password) => {
    const errorMsgs = {
      length: password.length < 6 ? "La contraseña debe tener al menos 6 caracteres." : "",
      maxLength: password.length > 10 ? "La contraseña no puede tener más de 10 caracteres." : "",
      uppercase: !/[A-Z]/.test(password) ? "La contraseña debe contener al menos una letra mayúscula." : "",
      lowercase: !/[a-z]/.test(password) ? "La contraseña debe contener al menos una letra minúscula." : "",
      number: !/[0-9]/.test(password) ? "La contraseña debe contener al menos un número." : "",
      specialChar: !new RegExp(`[${SPECIAL_CHARACTERS.replace(/[-/\\^$*+?.()|[\]{}]/g, '\\$&')}]`).test(password)
        ? `La contraseña debe contener al menos un carácter especial: ${SPECIAL_CHARACTERS}`
        : ""
    };

    return errorMsgs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormSubmitted(true);

    setErrorMessages({ user: "", password: "" });

    if (!user.trim()) {
      setErrorMessages(prev => ({ ...prev, user: "Por favor, ingrese su nombre de usuario." }));
      return;
    }

    if (noUsers) {
      try {
        const response = await axios.post("http://localhost:8080/api/cloud/users", {
          username: user,
          password: password,
          password_changed: false,
          rol: "ADMINISTRADOR",
          lastname: "",
          email: "",
          department: ""
        });

        if (response.status === 201) {
          setIsChangingPassword(true);
          console.log("Usuario registrado exitosamente. Cambio de contraseña requerido.");
        }
      } catch (error) {
        console.error("Error al registrar usuario:", error.response);
        setErrorMessages(prev => ({
          ...prev,
          user: error.response?.data || "No se pudo conectar al servidor."
        }));
      }
    } else {
      try {
        const response = await axios.post("http://localhost:8080/api/cloud/authenticate", {
          username: user,
          password: password,
        });

        if (response.status === 200) {
          const { role, requirePasswordChange, username } = response.data;

          //guardar en localStorage
          localStorage.setItem("userRole", role);
          localStorage.setItem("username", username);

          if (requirePasswordChange) {
            setIsChangingPassword(true);
          } else {
            console.log("Inicio de sesión exitoso");
            navigate({ to: "/home" });
          }
        }
      } catch (error) {
        console.error("Error al iniciar sesión:", error.response);
        setErrorMessages(prev => ({ ...prev, user: "Usuario o contraseña incorrectos." }));
      }
    }
  };

  const handleChangePassword = async (e) => {
    e.preventDefault();
    setFormSubmitted(true);

    setErrorMessages({ user: "", password: "" });

    if (newPassword !== confirmPassword) {
      setErrorMessages(prev => ({ ...prev, password: "Las contraseñas no coinciden" }));
      return;
    }

    const passwordErrors = validarPassword(newPassword);
    const combinedPasswordErrors = Object.values(passwordErrors).filter(msg => msg).join(" ");

    if (combinedPasswordErrors) {
      setErrorMessages(prev => ({ ...prev, password: combinedPasswordErrors }));
      return;
    }

    try {
      const response = await axios.put(
        `http://localhost:8080/api/cloud/users/${user}/change-password`,
        { newPassword: newPassword }
      );

      if (response.status === 200) {
        //recuperar información del usuario después de cambiar la contraseña
        const userResponse = await axios.get(
          `http://localhost:8080/api/cloud/users/${user}`
        );

        if (userResponse.status === 200) {
          const { rol, username } = userResponse.data;
          localStorage.setItem("userRole", rol);
          localStorage.setItem("username", username);
          setIsChangingPassword(false);
          setNewPassword("");
          setConfirmPassword("");
          console.log("Contraseña cambiada exitosamente.");
          navigate({ to: "/home" });
        }
      }
    } catch (error) {
      setErrorMessages(prev => ({
        ...prev,
        password: error.response?.data || "Error al cambiar la contraseña."
      }));
    }
  };

  const ErrorMessageBox = ({ message }) => (
    message ? (
      <div className="bg-red-50 border-l-4 border-red-500 text-red-700 p-4 mt-4 rounded-r-md shadow-sm" role="alert">
        <p className="font-bold">Error:</p>
        <p>{message}</p>
      </div>
    ) : null
  );

  if (noUsers === null) {
    return null;
  }

  return (
    <div className="flex flex-col items-center justify-between w-screen h-screen bg-slate-100">
      <div className="w-full flex justify-start p-4">
        <img src={logo} className="w-[130px] p-2 h-auto" alt="logo empresa" />
      </div>
      <div className="flex items-center justify-center w-screen h-screen bg-slate-100">
        {isChangingPassword ? (
          <form
            className="flex flex-col items-center justify-center w-[350px] bg-slate-100 p-12 text-[15px] text-center box-content"
            onSubmit={handleChangePassword}
          >
            <div className="flex flex-col space-y-5">
              <h2 className="font-bold">Cambio de contraseña requerido</h2>

              {/* Nueva contraseña con botón Show/Hide */}
              <div className="relative">
                <input
                  className="py-[10px] px-4 border border-gray-300 rounded-md text-gray-700 w-full"
                  type={showNewPassword ? "text" : "password"}
                  value={newPassword}
                  placeholder="Nueva contraseña"
                  onChange={(e) => setNewPassword(e.target.value)}
                />
                <button
                  type="button"
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-sm text-[#0B6A8D] hover:text-[#357D9E]"
                  onClick={() => setShowNewPassword(prev => !prev)}
                >
                  {showNewPassword ? "Hide" : "Show"}
                </button>
              </div>

              {/* Confirmar contraseña con mismo botón */}
              <div className="relative">
                <input
                  className="py-[10px] px-4 border border-gray-300 rounded-md text-gray-700 w-full"
                  type={showNewPassword ? "text" : "password"}
                  value={confirmPassword}
                  placeholder="Confirmar contraseña"
                  onChange={(e) => setConfirmPassword(e.target.value)}
                />
                <button
                  type="button"
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-sm text-[#0B6A8D] hover:text-[#357D9E]"
                  onClick={() => setShowNewPassword(prev => !prev)}
                >
                  {showNewPassword ? "Hide" : "Show"}
                </button>
              </div>

              <button
                type="submit"
                className="py-[10px] px-6 mt-4 font-bold text-white bg-[#3F9BB9] rounded-md"
              >
                Cambiar contraseña
              </button>

              {formSubmitted && errorMessages.password && (
                <ErrorMessageBox message={errorMessages.password} />
              )}
            </div>

          </form>
        ) : (
          <form
            className="flex flex-col items-center justify-center w-[350px] bg-slate-100 p-8 text-[15px] text-center"
            onSubmit={handleSubmit}
          >
            <div className="flex flex-col space-y-4">
              <h2 className="font-bold text-gray-800 text-[18px] py-10">
                Welcome to STC APP WEB
              </h2>
              <input
                className="py-[10px] px-4 border border-gray-300 rounded-md text-gray-700 w-full"
                value={user}
                placeholder="User"
                onChange={(e) => setUser(e.target.value)}
                disabled={noUsers}
              />

              {/* Input de contraseña con botón para mostrar/ocultar */}
              <div className="relative">
                <input
                  className="py-[10px] px-4 border border-gray-300 rounded-md text-gray-700 w-full"
                  type={showPassword ? "text" : "password"}
                  value={noUsers ? "Stc20@" : password}
                  placeholder="Password"
                  onChange={(e) => setPassword(e.target.value)}
                  disabled={noUsers}
                />
                <button
                  type="button"
                  className="absolute right-2 top-1/2 -translate-y-1/2 text-sm text-[#0B6A8D] hover:text-[#357D9E]"
                  onClick={() => setShowPassword(prev => !prev)}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>

              <button
                type="submit"
                className="w-full py-[10px] mt-4 font-bold text-white bg-[#3F9BB9] rounded-md hover:bg-[#357D9E] transition duration-300"
              >
                {noUsers ? "Registrar" : "Iniciar sesión"}
              </button>

              {formSubmitted && (errorMessages.user || errorMessages.password) && (
                <ErrorMessageBox message={errorMessages.user || errorMessages.password} />
              )}
            </div>

          </form>
        )}
      </div>
      <footer className="w-full py-2 text-[10pt] text-center bg-[#3F9BB9] text-black">
        <p>Copyright SpeedToCloud © 2025. All rights reserved.</p>
      </footer>
    </div>
  );
};

export default Login;
