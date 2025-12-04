"use client";

import { useEffect, useState } from "react";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
    Select,
    SelectContent,
    SelectItem,
    SelectTrigger,
    SelectValue,
} from "@/components/ui/select";
import {
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableHeader,
    TableRow,
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { MoreHorizontal, Plus, RefreshCw, Trash2, Construction, CheckCircle2 } from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";

type Piste = {
    id: string;
    identifiant: string;
    longueurM: number;
    etat: string;
};

type CreatePisteRequest = {
    identifiant: string;
    longueurM: number;
    etat?: string;
};

type UpdatePisteEtatRequest = {
    etat: string;
};

const PISTE_ETATS = ["LIBRE", "OCCUPEE", "MAINTENANCE"] as const;

async function api<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, {
        ...init,
        headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
        cache: "no-store",
    });
    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(text || `HTTP ${res.status}`);
    }
    return res.json();
}

export default function PistePage() {
    const [items, setItems] = useState<Piste[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [onlyDisponibles, setOnlyDisponibles] = useState(false);

    const [openCreate, setOpenCreate] = useState(false);
    const [openEtat, setOpenEtat] = useState<{ id: string; etat: string } | null>(null);

    const [createForm, setCreateForm] = useState<CreatePisteRequest>({
        identifiant: "",
        longueurM: 1000,
        etat: "LIBRE",
    });

    const [etatForm, setEtatForm] = useState<UpdatePisteEtatRequest>({ etat: "LIBRE" });

    const load = async (only = false) => {
        try {
            setLoading(true);
            setError(null);
            const data = await api<Piste[]>(only ? "/api/pistes/disponibles" : "/api/pistes");
            setItems(data);
        } catch (e: any) {
            setError(e.message || "Erreur");
        }
        setLoading(false);
    };

    useEffect(() => {
        load(onlyDisponibles);
    }, [onlyDisponibles]);

    const badgeForEtat = (etat: string) => {
        const e = etat.toUpperCase();
        if (e === "LIBRE") return <Badge className="bg-green-600">{etat}</Badge>;
        if (e === "OCCUPEE") return <Badge className="bg-yellow-600">{etat}</Badge>;
        if (e === "MAINTENANCE") return <Badge className="bg-red-600">{etat}</Badge>;
        return <Badge variant="secondary">{etat}</Badge>;
    };

    const createPiste = async () => {
        const payload: CreatePisteRequest = {
            identifiant: createForm.identifiant.trim(),
            longueurM: Number(createForm.longueurM),
            etat: createForm.etat,
        };
        await api("/api/pistes", {
            method: "POST",
            body: JSON.stringify(payload),
        });
        setOpenCreate(false);
        setCreateForm({ identifiant: "", longueurM: 1000, etat: "LIBRE" });
        load(onlyDisponibles);
    };

    const openChangeEtat = (p: Piste) => {
        setEtatForm({ etat: p.etat });
        setOpenEtat({ id: p.id, etat: p.etat });
    };

    const updateEtat = async () => {
        if (!openEtat) return;
        const payload = { etat: etatForm.etat };
        await api(`/api/pistes/${openEtat.id}/etat`, {
            method: "PATCH",
            body: JSON.stringify(payload),
        });
        setOpenEtat(null);
        load(onlyDisponibles);
    };

    const deletePiste = async (id: string) => {
        if (!confirm("Supprimer cette piste ?")) return;
        await fetch(`${API_BASE}/api/pistes/${id}`, { method: "DELETE" });
        load(onlyDisponibles);
    };

    return (
        <div className="mx-auto max-w-6xl px-4 py-8 space-y-6">
            <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                    <h1 className="text-2xl font-semibold">Pistes</h1>
                </div>
                <div className="flex flex-wrap items-center gap-2">
                    <Button
                        variant={onlyDisponibles ? "default" : "outline"}
                        onClick={() => setOnlyDisponibles((v) => !v)}
                    >
                        {onlyDisponibles ? (
                            <>
                                <CheckCircle2 className="mr-2 h-4 w-4" />
                                Pistes libres
                            </>
                        ) : (
                            <>
                                <Construction className="mr-2 h-4 w-4" />
                                Toutes les pistes
                            </>
                        )}
                    </Button>
                    <Button variant="outline" onClick={() => load(onlyDisponibles)}>
                        <RefreshCw className="mr-2 h-4 w-4" />
                        Actualiser
                    </Button>
                    <Button onClick={() => setOpenCreate(true)}>
                        <Plus className="mr-2 h-4 w-4" />
                        Nouvelle piste
                    </Button>
                </div>
            </div>

            {error && <Card className="border-red-300 bg-red-50 p-4 text-red-800">{error}</Card>}

            <Card className="overflow-hidden py-0">
                <div className="overflow-x-auto px-4 py-2">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Identifiant</TableHead>
                                <TableHead>Longueur (m)</TableHead>
                                <TableHead>État</TableHead>
                                <TableHead className="text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={4}>Chargement…</TableCell>
                                </TableRow>
                            ) : items.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={4}>Aucune piste</TableCell>
                                </TableRow>
                            ) : (
                                items.map((p) => (
                                    <TableRow key={p.id}>
                                        <TableCell>{p.identifiant}</TableCell>
                                        <TableCell>{p.longueurM}</TableCell>
                                        <TableCell>{badgeForEtat(p.etat)}</TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button size="icon" variant="ghost">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem onClick={() => openChangeEtat(p)}>
                                                        Changer l’état
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem
                                                        className="text-red-600"
                                                        onClick={() => deletePiste(p.id)}
                                                    >
                                                        <Trash2 className="mr-2 h-4 w-4" /> Supprimer
                                                    </DropdownMenuItem>
                                                </DropdownMenuContent>
                                            </DropdownMenu>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </div>
            </Card>

            <Dialog open={openCreate} onOpenChange={setOpenCreate}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Nouvelle piste</DialogTitle>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>Identifiant</Label>
                            <Input
                                value={createForm.identifiant}
                                onChange={(e) =>
                                    setCreateForm((f) => ({ ...f, identifiant: e.target.value }))
                                }
                            />
                        </div>

                        <div className="space-y-2">
                            <Label>Longueur (mètres)</Label>
                            <Input
                                type="number"
                                min={1}
                                value={createForm.longueurM}
                                onChange={(e) =>
                                    setCreateForm((f) => ({ ...f, longueurM: Number(e.target.value) }))
                                }
                            />
                        </div>

                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select
                                value={createForm.etat}
                                onValueChange={(v) =>
                                    setCreateForm((f) => ({ ...f, etat: v }))
                                }
                            >
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    {PISTE_ETATS.map((e) => (
                                        <SelectItem key={e} value={e}>
                                            {e}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setOpenCreate(false)}>
                            Annuler
                        </Button>
                        <Button onClick={createPiste}>Enregistrer</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={!!openEtat} onOpenChange={(o) => !o && setOpenEtat(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Changer l’état de la piste</DialogTitle>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select
                                value={etatForm.etat}
                                onValueChange={(v) => setEtatForm({ etat: v })}
                            >
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    {PISTE_ETATS.map((e) => (
                                        <SelectItem key={e} value={e}>
                                            {e}
                                        </SelectItem>
                                    ))}
                                </SelectContent>
                            </Select>
                        </div>
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setOpenEtat(null)}>
                            Annuler
                        </Button>
                        <Button onClick={updateEtat}>Mettre à jour</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
