"use client";

import { useEffect, useState } from "react";
import {
    Button,
} from "@/components/ui/button";
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
import {
    MoreHorizontal,
    Plus,
    Pencil,
    Trash2,
    RefreshCw,
    Plane,
} from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";

// ---------- Types ----------
type Hangar = {
    id: string;
    identifiant: string;
    capacite: number;
    etat: string;
};

type Avion = {
    id: string;
    immatriculation: string;
    type: string;
    capacite: number;
    etat: string;
};

// ---------- Helper fetch ----------
async function api<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, {
        ...init,
        headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
        cache: "no-store",
    });
    if (!res.ok) {
        throw new Error(await res.text().catch(() => "Erreur HTTP"));
    }
    return res.json();
}

// ---------- Page ----------
export default function HangarPage() {
    const [items, setItems] = useState<Hangar[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    const [openCreate, setOpenCreate] = useState(false);
    const [openEdit, setOpenEdit] = useState<Hangar | null>(null);
    const [openViewAvions, setOpenViewAvions] = useState<Hangar | null>(null);
    const [avions, setAvions] = useState<Avion[]>([]);

    const [form, setForm] = useState({
        identifiant: "",
        capacite: 0,
        etat: "DISPONIBLE",
    });

    // --- load hangars ---
    const load = async () => {
        try {
            setLoading(true);
            setError(null);
            const data = await api<Hangar[]>("/api/hangars");
            setItems(data);
        } catch (e: any) {
            setError(e.message || "Erreur de chargement");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, []);

    // --- helpers ---
    const setField = (k: keyof typeof form, v: any) =>
        setForm((f) => ({ ...f, [k]: v }));

    const startCreate = () => {
        setForm({ identifiant: "", capacite: 0, etat: "DISPONIBLE" });
        setOpenCreate(true);
    };

    const startEdit = (h: Hangar) => {
        setForm({
            identifiant: h.identifiant,
            capacite: h.capacite,
            etat: h.etat,
        });
        setOpenEdit(h);
    };

    const createHangar = async () => {
        const body = {
            identifiant: form.identifiant.trim(),
            capacite: Number(form.capacite),
            etat: form.etat,
        };
        await api<Hangar>("/api/hangars", {
            method: "POST",
            body: JSON.stringify(body),
        });
        setOpenCreate(false);
        await load();
    };

    const updateHangar = async (id: string) => {
        const body = {
            capacite: Number(form.capacite),
            etat: form.etat,
        };
        await api<Hangar>(`/api/hangars/${id}`, {
            method: "PUT",
            body: JSON.stringify(body),
        });
        setOpenEdit(null);
        await load();
    };

    const deleteHangar = async (id: string) => {
        if (!confirm("Supprimer ce hangar ?")) return;
        await fetch(`${API_BASE}/api/hangars/${id}`, { method: "DELETE" });
        await load();
    };

    const viewAvions = async (h: Hangar) => {
        setOpenViewAvions(h);
        try {
            const data = await api<Avion[]>(`/api/hangars/${h.id}/avions`);
            setAvions(data);
        } catch (e: any) {
            alert(e.message || "Erreur lors du chargement des avions");
        }
    };

    const badgeForEtat = (etat: string) => {
        const n = etat.toUpperCase();
        if (n.includes("DISP")) return <Badge className="bg-green-600">{etat}</Badge>;
        if (n.includes("OCCU")) return <Badge className="bg-yellow-600">{etat}</Badge>;
        if (n.includes("MAINT")) return <Badge className="bg-red-600">{etat}</Badge>;
        return <Badge variant="secondary">{etat}</Badge>;
    };

    return (
        <div className="mx-auto max-w-6xl px-4 py-8 space-y-6">
            {/* Header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-semibold">Hangars</h1>
                    <p className="text-muted-foreground">
                        Gestion des hangars et des avions qu’ils abritent.
                    </p>
                </div>
                <div className="flex gap-2">
                    <Button variant="outline" onClick={load}>
                        <RefreshCw className="mr-2 h-4 w-4" />
                        Actualiser
                    </Button>
                    <Button onClick={startCreate}>
                        <Plus className="mr-2 h-4 w-4" />
                        Nouveau hangar
                    </Button>
                </div>
            </div>

            {error && (
                <Card className="border-red-300 bg-red-50 p-4 text-red-800">{error}</Card>
            )}

            {/* Table */}
            <Card className="overflow-hidden py-0">
                <div className="overflow-x-auto px-4 py-2">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Identifiant</TableHead>
                                <TableHead>Capacité</TableHead>
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
                                    <TableCell colSpan={4} className="text-muted-foreground">
                                        Aucun hangar
                                    </TableCell>
                                </TableRow>
                            ) : (
                                items.map((h) => (
                                    <TableRow key={h.id}>
                                        <TableCell className="font-medium">{h.identifiant}</TableCell>
                                        <TableCell>{h.capacite}</TableCell>
                                        <TableCell>{badgeForEtat(h.etat)}</TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button size="icon" variant="ghost">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem onClick={() => startEdit(h)}>
                                                        <Pencil className="mr-2 h-4 w-4" /> Modifier
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem onClick={() => viewAvions(h)}>
                                                        <Plane className="mr-2 h-4 w-4" /> Voir avions
                                                    </DropdownMenuItem>
                                                    <DropdownMenuItem
                                                        className="text-red-600"
                                                        onClick={() => deleteHangar(h.id)}
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

            {/* CREATE */}
            <Dialog open={openCreate} onOpenChange={setOpenCreate}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Nouveau hangar</DialogTitle>
                        <DialogDescription>Renseignez les informations.</DialogDescription>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>Identifiant</Label>
                            <Input
                                value={form.identifiant}
                                onChange={(e) => setField("identifiant", e.target.value)}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label>Capacité</Label>
                            <Input
                                type="number"
                                min={0}
                                value={form.capacite}
                                onChange={(e) => setField("capacite", e.target.value)}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select value={form.etat} onValueChange={(v) => setField("etat", v)}>
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="DISPONIBLE">DISPONIBLE</SelectItem>
                                    <SelectItem value="OCCUPE">OCCUPE</SelectItem>
                                    <SelectItem value="EN_MAINTENANCE">EN_MAINTENANCE</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setOpenCreate(false)}>
                            Annuler
                        </Button>
                        <Button onClick={createHangar}>Enregistrer</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* EDIT */}
            <Dialog open={!!openEdit} onOpenChange={(o) => !o && setOpenEdit(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Modifier le hangar</DialogTitle>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>Identifiant</Label>
                            <Input value={form.identifiant} disabled />
                        </div>
                        <div className="space-y-2">
                            <Label>Capacité</Label>
                            <Input
                                type="number"
                                min={0}
                                value={form.capacite}
                                onChange={(e) => setField("capacite", e.target.value)}
                            />
                        </div>
                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select value={form.etat} onValueChange={(v) => setField("etat", v)}>
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="DISPONIBLE">DISPONIBLE</SelectItem>
                                    <SelectItem value="OCCUPE">OCCUPE</SelectItem>
                                    <SelectItem value="EN_MAINTENANCE">EN_MAINTENANCE</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => setOpenEdit(null)}>
                            Annuler
                        </Button>
                        {openEdit && (
                            <Button onClick={() => updateHangar(openEdit.id)}>Enregistrer</Button>
                        )}
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* VIEW AVIONS */}
            <Dialog open={!!openViewAvions} onOpenChange={(o) => !o && setOpenViewAvions(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Avions dans le hangar {openViewAvions?.identifiant}</DialogTitle>
                    </DialogHeader>
                    {avions.length === 0 ? (
                        <p className="text-sm text-muted-foreground">Aucun avion assigné.</p>
                    ) : (
                        <ul className="space-y-2">
                            {avions.map((a) => (
                                <li
                                    key={a.id}
                                    className="flex items-center justify-between rounded-md border p-2"
                                >
                                    <span className="font-medium">{a.immatriculation}</span>
                                    <Badge variant="secondary">{a.type}</Badge>
                                </li>
                            ))}
                        </ul>
                    )}
                    <DialogFooter>
                        <Button onClick={() => setOpenViewAvions(null)}>Fermer</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
