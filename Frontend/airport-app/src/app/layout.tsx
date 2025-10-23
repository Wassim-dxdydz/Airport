import "./globals.css";
import type { Metadata } from "next";
import { Toaster } from "@/components/ui/toaster";
import Navbar from "@/components/site/Navbar";
import Footer from "@/components/site/Footer";


export const metadata: Metadata = {
    title: "MiagePort",
    description: "Airport frontend (vols, avions, hangars, pistes)",
};


export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="en" suppressHydrationWarning>
        <body className="min-h-screen bg-background text-foreground antialiased flex flex-col">
        <Navbar />
        <main className="flex-1">{children}</main>
        <Footer />
        <Toaster />
        </body>
        </html>
    );
}