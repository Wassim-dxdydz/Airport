// src/app/layout.tsx
import type { Metadata } from "next";
import "./globals.css";
import { SiteHeader } from "@/components/site-header";
import { SiteFooter } from "@/components/site-footer";

export const metadata: Metadata = {
    title: "AéroOps",
    description: "Gestion des avions, hangars, pistes et vols.",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
    return (
        <html lang="fr" suppressHydrationWarning>
        <body className="min-h-dvh bg-background text-foreground antialiased pt-16 overflow-x-hidden">
        <SiteHeader />
        <main>{children}</main>
        <SiteFooter />
        </body>
        </html>
    );
}
