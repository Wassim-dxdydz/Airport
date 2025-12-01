"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { ArrowRight } from "lucide-react";
import { Button } from "@/components/ui/button";

const WORDS = ["Avions", "Hangars", "Pistes", "Vols"];

export function HeroClient() {
    const [index, setIndex] = useState(0);
    const [subText, setSubText] = useState("");
    const [deleting, setDeleting] = useState(false);

    // Slower, smoother typing animation
    useEffect(() => {
        const currentWord = WORDS[index];
        const speed = deleting ? 100 : 180;

        const timeout = setTimeout(() => {
            if (!deleting && subText.length < currentWord.length) {
                setSubText(currentWord.slice(0, subText.length + 1));
            } else if (deleting && subText.length > 0) {
                setSubText(currentWord.slice(0, subText.length - 1));
            } else if (!deleting && subText.length === currentWord.length) {
                setTimeout(() => setDeleting(true), 1000); // pause before delete
            } else if (deleting && subText.length === 0) {
                setDeleting(false);
                setIndex((i) => (i + 1) % WORDS.length);
            }
        }, speed);

        return () => clearTimeout(timeout);
    }, [subText, deleting, index]);

    return (
        <div className="absolute inset-0 flex items-center">
            <div className="mx-auto w-full max-w-6xl px-4">
                <div className="max-w-2xl space-y-6 text-white">
                    <h1 className="text-4xl font-bold leading-tight md:text-6xl">
                        Pilotez vos opérations aéroportuaires.
                    </h1>
                    <p className="text-white/90 text-lg md:text-xl">
                        Gérez vos{" "}
                        <span className="font-semibold text-primary-foreground">
              {subText}
                            <span className="animate-pulse ml-0.5">|</span>
            </span>{""}
                        avec cohérence et rapidité.
                    </p>
                    <div className="flex gap-3">
                        <a href="#modules">
                            <Button size="lg" className="bg-white text-black hover:bg-white/90">
                                Commencer
                                <ArrowRight className="ml-2 h-4 w-4" />
                            </Button>
                        </a>
                    </div>
                </div>
            </div>
        </div>
    );
}
