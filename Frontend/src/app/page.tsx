import Image from "next/image";
import Link from "next/link";
import { ArrowRight, Plane, Warehouse, Route, CalendarClock, Users, ClipboardCheck } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { HeroClient } from "@/components/hero-client";

export const dynamic = "force-dynamic";

const HERO_IMAGES = ["/hero1.jpg", "/hero2.jpg", "/hero3.jpg", "/hero4.jpg", "/hero5.jpg"];

const features = [
    { href: "/avion", title: "Avion", desc: "Inventaire, état, maintenance.", icon: Plane },
    { href: "/hangar", title: "Hangar", desc: "Occupation, affectations et accès.", icon: Warehouse },
    { href: "/piste", title: "Piste", desc: "Statut des pistes, créneaux et contraintes.", icon: Route },
    { href: "/vol", title: "Vol", desc: "Planification, équipage et allocations.", icon: CalendarClock },
    { href: "/passager", title: "Passagers", desc: "Gestion des clients et historique des vols.", icon: Users },
    { href: "/checkin", title: "Check-ins", desc: "Enregistrement des passagers.", icon: ClipboardCheck },
];

export default function Page() {
    const heroSrc = HERO_IMAGES[Math.floor(Math.random() * HERO_IMAGES.length)];

    return (
        <div className="space-y-16">
            <section className="relative h-[100svh] w-screen -mt-16 overflow-hidden">
                <Image src={heroSrc} alt="Aéroport" fill priority className="object-cover" sizes="100vw" />
                <div className="absolute inset-0 bg-gradient-to-b from-black/50 via-black/30 to-black/60" />

                <HeroClient />
            </section>
            <section id="modules" className="scroll-mt-20 mx-auto grid max-w-6xl gap-6 px-4 md:grid-cols-2 lg:grid-cols-3">
                {features.map(({ href, title, desc, icon: Icon }) => (
                    <Link key={href} href={href}>
                        <Card className="transition-shadow hover:shadow-lg">
                            <CardHeader className="flex flex-row items-center gap-3">
                                <div className="rounded-lg border p-2">
                                    <Icon className="h-5 w-5" />
                                </div>
                                <div>
                                    <CardTitle>{title}</CardTitle>
                                    <CardDescription>{desc}</CardDescription>
                                </div>
                            </CardHeader>
                            <CardContent>
                                <Button variant="secondary">
                                    Ouvrir {title} <ArrowRight className="ml-2 h-4 w-4" />
                                </Button>
                            </CardContent>
                        </Card>
                    </Link>
                ))}
            </section>
        </div>
    );
}
