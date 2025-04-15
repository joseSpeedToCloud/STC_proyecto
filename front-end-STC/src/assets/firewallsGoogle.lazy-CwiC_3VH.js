import{r as s,j as e,c as b}from"./index-BXzBXnnT.js";import{S as i}from"./Sidebar-D33F3K_E.js";function p(){const[a,c]=s.useState(""),[l,o]=s.useState([]);s.useEffect(()=>{(async()=>{try{const n=await(await fetch("http://localhost:8080/api/cloud/firewallsgoogle")).json();o(n)}catch(t){console.error("Error al obtener los datos:",t)}})()},[]);const d=r=>{c(r.target.value)},x=l.filter(r=>Object.values(r).some(t=>String(t).toLowerCase().includes(a.toLowerCase())));return e.jsx(i,{children:e.jsx("div",{className:"flex flex-col h-screen",children:e.jsxs("div",{className:"w-full flex flex-col flex-grow",children:[e.jsx("div",{className:"flex justify-end mb-4 space-x-2",children:e.jsx("input",{placeholder:"Filter",className:"border-2 border-[#ccc] rounded-[5px] px-3 py-2 text-sm text-black",value:a,onChange:d})}),e.jsx("h1",{className:"text-2xl mb-6 ml-6 text-[#3F9BB9]",children:"Firewalls"}),e.jsxs("table",{className:"min-w-full bg-white rounded-lg",children:[e.jsx("thead",{children:e.jsxs("tr",{className:"bg-gray-200 text-[#0B6A8D] font-semibold",children:[e.jsx("td",{className:"py-2 px-4 border-b border-gray-200 text-center",children:"ID"}),e.jsx("td",{className:"py-2 px-4 border-b border-gray-200 text-center",children:"Name"}),e.jsx("td",{className:"py-2 px-4 border-b border-gray-200 text-center",children:"Project_id"}),e.jsx("td",{className:"py-2 px-4 border-b border-gray-200 text-center",children:"direction"}),e.jsx("td",{className:"py-2 px-4 border-b border-gray-200 text-center",children:"priority"}),e.jsx("td",{className:"py-2 px-4 border-b border-gray-200 text-center",children:"protocol"}),e.jsx("td",{className:"py-2 px-4 border-b border-gray-200 text-center",children:"source_ranges"}),e.jsx("td",{className:"py-2 px-4 border-b border-gray-200 text-center",children:"target_tags"})]})}),e.jsx("tbody",{children:x.map((r,t)=>e.jsxs("tr",{children:[e.jsx("td",{className:"text-sm text-center py-2 px-4 border-b border-gray-200",children:r.firewall_id}),e.jsx("td",{className:"text-sm text-center py-2 px-4 border-b border-gray-200",children:r.name}),e.jsx("td",{className:"text-sm text-center py-2 px-4 border-b border-gray-200",children:r.project_id}),e.jsx("td",{className:"text-sm text-center py-2 px-4 border-b border-gray-200",children:r.direction}),e.jsx("td",{className:"text-sm text-center py-2 px-4 border-b border-gray-200",children:r.priority}),e.jsx("td",{className:"text-sm text-center py-2 px-4 border-b border-gray-200",children:r.protocol}),e.jsx("td",{className:"text-sm text-center py-2 px-4 border-b border-gray-200",children:r.source_ranges}),e.jsx("td",{className:"text-sm text-center py-2 px-4 border-b border-gray-200",children:r.target_tags})]},t))})]})]})})})}const m=b("/firewallsGoogle")({component:p});export{m as Route};
