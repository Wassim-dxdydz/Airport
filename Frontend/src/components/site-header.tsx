// src/components/site-header.tsx
"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import { Menu } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
    Sheet,
    SheetContent,
    SheetHeader,
    SheetTitle,
    SheetTrigger,
} from "@/components/ui/sheet";
import { cn } from "@/lib/utils";

const NAV = [
    { href: "/avion", label: "Avion" },
    { href: "/hangar", label: "Hangar" },
    { href: "/piste", label: "Piste" },
    { href: "/vol", label: "Vol" },
];

export function SiteHeader() {
    const pathname = usePathname();
    const isHome = pathname === "/";
    const [atTop, setAtTop] = useState(true);

    useEffect(() => {
        const onScroll = () => setAtTop(window.scrollY < 8);
        onScroll();
        window.addEventListener("scroll", onScroll, { passive: true });
        return () => window.removeEventListener("scroll", onScroll);
    }, [pathname]);

    const translucent = isHome && atTop;

    return (
        <header
            className={cn(
                "fixed top-0 left-0 right-0 z-40 h-16 border-b transition-colors duration-300",
                translucent ? "bg-transparent border-transparent" : "bg-background/80 backdrop-blur border-border"
            )}
        >
            <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4">
                {/* Left: Logo */}
                <Link
                    href="/"
                    className={cn(
                        "flex items-center gap-2",
                        translucent && "text-white"
                    )}
                >
          <span className={cn(
              "inline-flex h-8 w-8 items-center justify-center rounded-full border font-bold",
              translucent ? "border-white/70" : "border-foreground/20"
          )}>
            ✈️
          </span>
                    <span className="text-lg font-semibold tracking-tight">AéroOps</span>
                </Link>

                {/* Center: Nav with underline */}
                <nav className="hidden md:flex flex-1 justify-center gap-20">
                    {NAV.map((item) => {
                        const isActive = pathname.startsWith(item.href);
                        return (
                            <Link
                                key={item.href}
                                href={item.href}
                                className={cn(
                                    "relative text-sm font-medium transition-colors",
                                    translucent
                                        ? "text-white/90 hover:text-white"
                                        : "text-muted-foreground hover:text-primary",
                                    isActive && (translucent ? "text-white" : "text-primary")
                                )}
                            >
                                {item.label}
                                <span
                                    className={cn(
                                        "absolute left-0 -bottom-1 h-[2px] w-full origin-left scale-x-0 transition-transform duration-300",
                                        translucent ? "bg-white" : "bg-primary",
                                        isActive && "scale-x-100"
                                    )}
                                />
                            </Link>
                        );
                    })}
                </nav>

                {/* Right placeholder */}
                <div className="hidden md:block w-10" />

                {/* Mobile menu */}
                <div className="md:hidden">
                    <Sheet>
                        <SheetTrigger asChild>
                            <Button
                                variant={translucent ? "ghost" : "ghost"}
                                size="icon"
                                aria-label="Open menu"
                                className={cn(translucent && "text-white hover:text-white/90")}
                            >
                                <Menu className="h-5 w-5" />
                            </Button>
                        </SheetTrigger>
                        <SheetContent side="right" className="w-72">
                            <SheetHeader>
                                <SheetTitle className="text-left">Navigation</SheetTitle>
                            </SheetHeader>
                            <div className="mt-4 grid gap-2">
                                {NAV.map((item) => (
                                    <Link key={item.href} href={item.href}>
                                        <Button
                                            variant={pathname.startsWith(item.href) ? "default" : "ghost"}
                                            className="w-full justify-start"
                                        >
                                            {item.label}
                                        </Button>
                                    </Link>
                                ))}
                            </div>
                        </SheetContent>
                    </Sheet>
                </div>
            </div>
        </header>
    );
}
