import google from '../assets/logoGoogle.png'
import home from '../assets/home.svg'
import azure from '../assets/azure.svg'
import amazon from '../assets/amazon.svg'
import user from '../assets/user.svg'
import setting from '../assets/settings.svg'
import idioma from '../assets/idioma.svg'
import credenciales from '../assets/credenciales.svg'

export const SidebarData = [
    {
      title: 'Home',
      path: '/home',
      icon: home,
      cName: 'item-menu'
    },
    {
      title: 'Autenticación',
      path: '/loginToken', 
      icon: credenciales,
      cName: 'item-menu',
      roles: ['ADMINISTRADOR']
    },  
    {
        title: 'Google Cloud',
        icon: google,
        cName: 'item-menu',
        subLinks: [
            {
              title: 'Home GCP',
              path: '/homegcp'
            }
        ]
     },
    {
        title: 'Amazon AWS',
        icon: amazon,
        cName: 'item-menu',
        subLinks: [
            {
              title: 'Home AWS',
              path: '/homeaws'
            }
        ]
    },
    {
        title: 'Azure',
        icon: azure,
        cName: 'item-menu',
        subLinks: [
            {
              title: 'Home Azure',
              path: '/homeazure'
            }
        ]
    },
    {
      title: 'User',
      path: '/users',
      icon: user,
      cName: 'item-menu'
    },
    {
      title: 'Global Settings',
      path: '/settings',
      icon: setting,
      cName: 'item-menu'
    },
    {
      title: 'Languages',
      path: '/languages',
      icon: idioma,
      cName: 'item-menu'
    }
]

export default SidebarData;

