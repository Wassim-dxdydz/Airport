"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { Button } from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
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
    TableRow
} from "@/components/ui/table";
import { Badge } from "@/components/ui/badge";
import { Card } from "@/components/ui/card";
import { MoreHorizontal, Plus, Pencil, Trash2, Unlink2, RefreshCw } from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";

type Avion = {
    id: string;
    immatriculation: string;
    type: string;
    capacite: number;
    etat: string;
    hangarId?: string | null;
};

type CreateAvionRequest = {
    immatriculation: string;
    type: string;
    capacite: number;
    etat?: string;
    hangarId?: string | null;
};

type UpdateAvionRequest = {
    type?: string;
    capacite?: number;
    etat?: string;
    hangarId?: string | null;
};

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";

async function api<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, {
        ...init,
        headers: {
            "Content-Type": "application/json",
            ...(init?.headers || {}),
        },
        cache: "no-store",
    });
    if (!res.ok) {
        const text = await res.text().catch(() => "");
        throw new Error(`${res.status} ${res.statusText}: ${text || "Request failed"}`);
    }
    return res.json() as Promise<T>;
}

export default function AvionPage() {
    const [items, setItems] = useState<Avion[]>([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);

    const [openCreate, setOpenCreate] = useState(false);
    const [openEdit, setOpenEdit] = useState<Avion | null>(null);

    const [form, setForm] = useState({
        immatriculation: "",
        type: "",
        capacite: 1,
        etat: undefined as string | undefined,
        hangarId: null as string | null,
    });

    const load = async () => {
        setLoading(true);
        setErr(null);
        try {
            const data = await api<Avion[]>("/api/avions");
            setItems(data);
        } catch (e: any) {
            setErr(e?.message);
        }
        setLoading(false);
    };

    useEffect(() => {
        load();
    }, []);

    const resetForm = () => {
        setForm({
            immatriculation: "",
            type: "",
            capacite: 1,
            etat: undefined,
            hangarId: null,
        });
    };

    const startCreate = () => {
        resetForm();
        setOpenCreate(true);
    };

    const startEdit = (a: Avion) => {
        setForm({
            immatriculation: a.immatriculation,
            type: a.type,
            capacite: a.capacite,
            etat: a.etat,
            hangarId: a.hangarId ?? null,
        });
        setOpenEdit(a);
    };

    const createAvion = async () => {
        const payload: CreateAvionRequest = {
            immatriculation: form.immatriculation,
            type: form.type,
            capacite: Number(form.capacite),
            etat: form.etat,
            hangarId: form.hangarId,
        };
        await api<Avion>("/api/avions", {
            method: "POST",
            body: JSON.stringify(payload),
        });
        setOpenCreate(false);
        load();
    };

    const updateAvion = async (id: string) => {
        const payload: UpdateAvionRequest = {
            type: form.type,
            capacite: Number(form.capacite),
            etat: form.etat,
            hangarId: form.hangarId,
        };
        await api<Avion>(`/api/avions/${id}`, {
            method: "PATCH",
            body: JSON.stringify(payload),
        });
        setOpenEdit(null);
        load();
    };

    const deleteAvion = async (id: string) => {
        if (!confirm("Supprimer cet avion ?")) return;
        await fetch(`${API_BASE}/api/avions/${id}`, { method: "DELETE" });
        load();
    };

    const unassignHangar = async (id: string) => {
        await api<Avion>(`/api/avions/${id}/unassign-hangar`, { method: "POST" });
        load();
    };

    const badgeForEtat = (etat: string) => {
        const u = etat.toUpperCase();
        if (u === "EN_SERVICE") return <Badge className="bg-green-600">{etat}</Badge>;
        if (u === "EN_MAINTENANCE") return <Badge className="bg-yellow-600">{etat}</Badge>;
        if (u === "HORS_SERVICE") return <Badge className="bg-red-600">{etat}</Badge>;
        return <Badge>{etat}</Badge>;
    };

    const setField = (k: string, v: any) =>
        setForm((f) => ({ ...f, [k]: v }));

    return (
        <div className="mx-auto max-w-6xl px-4 py-8 space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-semibold">Avion</h1>
                </div>
                <div className="flex items-center gap-2">
                    <Button variant="outline" onClick={load}>
                        <RefreshCw className="mr-2 h-4 w-4" />
                        Actualiser
                    </Button>
                    <Button onClick={startCreate}>
                        <Plus className="mr-2 h-4 w-4" />
                        Nouvel avion
                    </Button>
                </div>
            </div>

            {err && <Card className="border-red-300 bg-red-50 p-4 text-red-800">{err}</Card>}

            <Card className="overflow-hidden py-0">
                <div className="overflow-x-auto px-4 py-2">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead>Immatriculation</TableHead>
                                <TableHead>Type</TableHead>
                                <TableHead>Capacité</TableHead>
                                <TableHead>État</TableHead>
                                <TableHead>Hangar</TableHead>
                                <TableHead className="w-20 text-right">Actions</TableHead>
                            </TableRow>
                        </TableHeader>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={6}>Chargement…</TableCell>
                                </TableRow>
                            ) : items.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={6}>Aucun avion.</TableCell>
                                </TableRow>
                            ) : (
                                items.map((a) => (
                                    <TableRow key={a.id}>
                                        <TableCell>{a.immatriculation}</TableCell>
                                        <TableCell>{a.type}</TableCell>
                                        <TableCell>{a.capacite}</TableCell>
                                        <TableCell>{badgeForEtat(a.etat)}</TableCell>
                                        <TableCell>{a.hangarId ?? "—"}</TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button size="icon" variant="ghost">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem onClick={() => startEdit(a)}>
                                                        <Pencil className="mr-2 h-4 w-4" /> Modifier
                                                    </DropdownMenuItem>
                                                    {a.hangarId && (
                                                        <DropdownMenuItem onClick={() => unassignHangar(a.id)}>
                                                            <Unlink2 className="mr-2 h-4 w-4" /> Retirer du hangar
                                                        </DropdownMenuItem>
                                                    )}
                                                    <DropdownMenuItem
                                                        className="text-red-600"
                                                        onClick={() => deleteAvion(a.id)}
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
                        <DialogTitle>Nouvel avion</DialogTitle>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Immatriculation</Label>
                                <Input
                                    value={form.immatriculation}
                                    onChange={(e) => setField("immatriculation", e.target.value)}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label>Type</Label>
                                <Input
                                    value={form.type}
                                    onChange={(e) => setField("type", e.target.value)}
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Capacité</Label>
                                <Input
                                    type="number"
                                    value={form.capacite}
                                    onChange={(e) => setField("capacite", e.target.value)}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label>État</Label>
                                <Select
                                    value={form.etat ?? ""}
                                    onValueChange={(v) => setField("etat", v || undefined)}
                                >
                                    <SelectTrigger>
                                        <SelectValue />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="EN_SERVICE">EN_SERVICE</SelectItem>
                                        <SelectItem value="MAINTENANCE">MAINTENANCE</SelectItem>
                                        <SelectItem value="HORS_SERVICE">HORS_SERVICE</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label>Hangar</Label>
                            <Input
                                value={form.hangarId ?? ""}
                                onChange={(e) => setField("hangarId", e.target.value || null)}
                            />
                        </div>
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setOpenCreate(false)}>Annuler</Button>
                        <Button onClick={createAvion}>Enregistrer</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={!!openEdit} onOpenChange={(open) => !open && setOpenEdit(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Modifier l’avion</DialogTitle>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>Immatriculation</Label>
                            <Input value={form.immatriculation} disabled />
                        </div>

                        <div className="space-y-2">
                            <Label>Type</Label>
                            <Input
                                value={form.type}
                                onChange={(e) => setField("type", e.target.value)}
                            />
                        </div>

                        <div className="space-y-2">
                            <Label>Capacité</Label>
                            <Input
                                type="number"
                                value={form.capacite}
                                onChange={(e) => setField("capacite", e.target.value)}
                            />
                        </div>

                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select
                                value={form.etat ?? ""}
                                onValueChange={(v) => setField("etat", v)}
                            >
                                <SelectTrigger>
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="EN_SERVICE">EN_SERVICE</SelectItem>
                                    <SelectItem value="MAINTENANCE">MAINTENANCE</SelectItem>
                                    <SelectItem value="HORS_SERVICE">HORS_SERVICE</SelectItem>
                                </SelectContent>
                            </Select>
                        </div>

                        <div className="space-y-2">
                            <Label>Hangar</Label>
                            <Input
                                value={form.hangarId ?? ""}
                                onChange={(e) => setField("hangarId", e.target.value || null)}
                            />
                        </div>
                    </div>

                    <DialogFooter>
                        <Button variant="outline" onClick={() => setOpenEdit(null)}>Annuler</Button>
                        <Button onClick={() => openEdit && updateAvion(openEdit.id)}>Enregistrer</Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
