"use client";
import Hero from "@/components/site/Hero";
import SectionCards from "@/components/site/SectionCards";


export default function Page() {
    return (
        <div className="w-full">
            <Hero />
            <SectionCards />
        </div>
    );
}


// =============================
// File: src/app/vol/page.tsx
// =============================
"use client";
import CrudPage from "@/components/crud/CrudPage";


export default function VolPage() {
    return (
        <CrudPage title="Vols" endpoint="/api/vols" idKey="id" />
    );
}