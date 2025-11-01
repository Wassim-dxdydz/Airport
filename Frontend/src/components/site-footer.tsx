import Link from "next/link";
import { Mail, MapPin, Phone, Github, Twitter, Linkedin } from "lucide-react";

export function SiteFooter() {
    return (
        <footer className="mt-24 border-t bg-background">
            <div className="mx-auto grid max-w-6xl gap-10 px-4 py-12 md:grid-cols-4">
                <div className="space-y-3">
                    <div className="flex items-center gap-2">
                        <span className="inline-flex h-8 w-8 items-center justify-center rounded-full border font-bold">✈️</span>
                        <span className="text-lg font-semibold">AéroOps</span>
                    </div>
                    <p className="text-sm text-muted-foreground">
                        Suite de gestion aéroportuaire pour les équipes opérations, maintenance et planning.
                    </p>
                </div>

                <div>
                    <h4 className="mb-3 font-semibold">Société</h4>
                    <ul className="space-y-2 text-sm text-muted-foreground">
                        <li><Link href="#" className="hover:text-foreground">À propos</Link></li>
                        <li><Link href="#" className="hover:text-foreground">Carrières</Link></li>
                        <li><Link href="#" className="hover:text-foreground">Presse</Link></li>
                        <li><Link href="#" className="hover:text-foreground">Sécurité</Link></li>
                    </ul>
                </div>

                <div>
                    <h4 className="mb-3 font-semibold">Ressources</h4>
                    <ul className="space-y-2 text-sm text-muted-foreground">
                        <li><Link href="#" className="hover:text-foreground">Documentation</Link></li>
                        <li><Link href="#" className="hover:text-foreground">Statut du service</Link></li>
                        <li><Link href="#" className="hover:text-foreground">Centre d’aide</Link></li>
                        <li><Link href="#" className="hover:text-foreground">API</Link></li>
                    </ul>
                </div>

                <div className="space-y-2 text-sm text-muted-foreground">
                    <h4 className="mb-3 font-semibold">Contact</h4>
                    <p className="flex items-center gap-2"><MapPin className="h-4 w-4" /> 12 rue des Aviateurs, 75015 Paris</p>
                    <p className="flex items-center gap-2"><Phone className="h-4 w-4" /> +33 1 86 76 45 12</p>
                    <p className="flex items-center gap-2"><Mail className="h-4 w-4" /> support@aeroops.example</p>
                    <div className="mt-3 flex gap-3 text-foreground">
                        <Link href="#" aria-label="Twitter"><Twitter className="h-4 w-4" /></Link>
                        <Link href="#" aria-label="LinkedIn"><Linkedin className="h-4 w-4" /></Link>
                        <Link href="#" aria-label="GitHub"><Github className="h-4 w-4" /></Link>
                    </div>
                </div>
            </div>
            <div className="border-t">
                <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-6 text-xs text-muted-foreground">
                    <p>© {new Date().getFullYear()} AéroOps SAS — Tous droits réservés.</p>
                    <div className="flex gap-4">
                        <Link href="#" className="hover:text-foreground">Conditions</Link>
                        <Link href="#" className="hover:text-foreground">Confidentialité</Link>
                        <Link href="#" className="hover:text-foreground">Cookies</Link>
                    </div>
                </div>
            </div>
        </footer>
    );
}
