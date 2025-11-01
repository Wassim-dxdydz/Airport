"use client";

import { useEffect, useMemo, useState } from "react";
import Link from "next/link";
import {
    Button
} from "@/components/ui/button";
import {
    Dialog,
    DialogContent,
    DialogHeader,
    DialogTitle,
    DialogDescription,
    DialogFooter,
    DialogTrigger,
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
import { MoreHorizontal, Plus, Pencil, Trash2, Link2, Unlink2, RefreshCw } from "lucide-react";
import {
    DropdownMenu,
    DropdownMenuContent,
    DropdownMenuItem,
    DropdownMenuTrigger
} from "@/components/ui/dropdown-menu";

// ---------- Types ----------
type Avion = {
    id: string;
    immatriculation: string;
    type: string;
    capacite: number;
    etat: string;           // e.g. EN_SERVICE (stringly typed to accept any backend enum)
    hangarId?: string | null;
};

type CreateAvionRequest = {
    immatriculation: string;
    type: string;
    capacite: number;
    etat?: string;          // optional, backend defaults to EN_SERVICE
    hangarId?: string | null;
};

type UpdateAvionRequest = Partial<Omit<CreateAvionRequest, "immatriculation">> & {
    // immatriculation not updatable in your backend spec (create-only)
};

// ---------- API base ----------
const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? ""; // e.g. "http://localhost:8080"

// Utility fetch wrapper
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

// ---------- Page ----------
export default function AvionPage() {
    const [items, setItems] = useState<Avion[]>([]);
    const [loading, setLoading] = useState(true);
    const [err, setErr] = useState<string | null>(null);

    const [openCreate, setOpenCreate] = useState(false);
    const [openEdit, setOpenEdit] = useState<Avion | null>(null);

    // form state (create/edit)
    const [form, setForm] = useState<{
        immatriculation: string;
        type: string;
        capacite: number | string;
        etat?: string;
        hangarId?: string | null;
    }>({
        immatriculation: "",
        type: "",
        capacite: 1,
        etat: undefined,
        hangarId: null,
    });

    const load = async () => {
        try {
            setLoading(true);
            setErr(null);
            const data = await api<Avion[]>("/api/avions");
            setItems(data);
        } catch (e: any) {
            setErr(e?.message || "Erreur de chargement");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        load();
    }, []);

    // --- helpers ---
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
            immatriculation: form.immatriculation.trim(),
            type: form.type.trim(),
            capacite: Number(form.capacite),
            ...(form.etat ? { etat: form.etat } : {}),
            ...(form.hangarId ? { hangarId: form.hangarId } : {}),
        };
        await api<Avion>("/api/avions", {
            method: "POST",
            body: JSON.stringify(payload),
        });
        setOpenCreate(false);
        await load();
    };

    const updateAvion = async (id: string) => {
        const payload: UpdateAvionRequest = {
            type: form.type.trim(),
            capacite: Number(form.capacite),
            etat: form.etat,
            hangarId: form.hangarId || null,
        };
        await api<Avion>(`/api/avions/${id}`, {
            method: "PUT",
            body: JSON.stringify(payload),
        });
        setOpenEdit(null);
        await load();
    };

    const deleteAvion = async (id: string) => {
        if (!confirm("Supprimer cet avion ?")) return;
        await fetch(`${API_BASE}/api/avions/${id}`, { method: "DELETE" });
        await load();
    };

    const unassignHangar = async (id: string) => {
        await api<Avion>(`/api/avions/${id}/unassign-hangar`, { method: "POST" });
        await load();
    };

    // Basic badge color mapping for state (fallback-safe)
    const badgeForEtat = (etat: string) => {
        const normalized = etat.toUpperCase();
        if (normalized.includes("SERVICE")) return <Badge className="bg-green-600 hover:bg-green-600">{etat}</Badge>;
        if (normalized.includes("MAINT")) return <Badge className="bg-yellow-600 hover:bg-yellow-600">{etat}</Badge>;
        if (normalized.includes("HORS")) return <Badge className="bg-red-600 hover:bg-red-600">{etat}</Badge>;
        return <Badge variant="secondary">{etat}</Badge>;
    };

    // Controlled input setters
    const setField = (k: keyof typeof form, v: any) => setForm((f) => ({ ...f, [k]: v }));

    return (
        <div className="mx-auto max-w-6xl px-4 py-8 space-y-6">
            {/* Page header */}
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-semibold">Avion</h1>
                    <p className="text-muted-foreground">Gestion de la flotte, fiches et affectations.</p>
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

            {/* Error */}
            {err && (
                <Card className="border-red-300 bg-red-50 p-4 text-red-800">
                    {err}
                </Card>
            )}

            {/* Table */}
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
                                    <TableCell colSpan={6} className="text-muted-foreground">
                                        Aucun avion. Créez-en un pour commencer.
                                    </TableCell>
                                </TableRow>
                            ) : (
                                items.map((a) => (
                                    <TableRow key={a.id}>
                                        <TableCell className="font-medium">{a.immatriculation}</TableCell>
                                        <TableCell>{a.type}</TableCell>
                                        <TableCell>{a.capacite}</TableCell>
                                        <TableCell>{badgeForEtat(a.etat)}</TableCell>
                                        <TableCell className="font-mono text-xs">
                                            {a.hangarId ?? <span className="text-muted-foreground">—</span>}
                                        </TableCell>
                                        <TableCell className="text-right">
                                            <DropdownMenu>
                                                <DropdownMenuTrigger asChild>
                                                    <Button size="icon" variant="ghost" aria-label="Actions">
                                                        <MoreHorizontal className="h-4 w-4" />
                                                    </Button>
                                                </DropdownMenuTrigger>
                                                <DropdownMenuContent align="end">
                                                    <DropdownMenuItem onClick={() => startEdit(a)}>
                                                        <Pencil className="mr-2 h-4 w-4" /> Modifier
                                                    </DropdownMenuItem>
                                                    {a.hangarId ? (
                                                        <DropdownMenuItem onClick={() => unassignHangar(a.id)}>
                                                            <Unlink2 className="mr-2 h-4 w-4" /> Retirer du hangar
                                                        </DropdownMenuItem>
                                                    ) : null}
                                                    <DropdownMenuItem className="text-red-600" onClick={() => deleteAvion(a.id)}>
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

            {/* CREATE dialog */}
            <Dialog open={openCreate} onOpenChange={setOpenCreate}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Nouvel avion</DialogTitle>
                        <DialogDescription>Renseignez les informations et enregistrez.</DialogDescription>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label htmlFor="immatriculation">Immatriculation</Label>
                                <Input
                                    id="immatriculation"
                                    value={form.immatriculation}
                                    onChange={(e) => setField("immatriculation", e.target.value)}
                                    placeholder="F-HABC"
                                />
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="type">Type</Label>
                                <Input
                                    id="type"
                                    value={form.type}
                                    onChange={(e) => setField("type", e.target.value)}
                                    placeholder="A320"
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label htmlFor="capacite">Capacité</Label>
                                <Input
                                    id="capacite"
                                    type="number"
                                    min={1}
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
                                        <SelectValue placeholder="(par défaut: EN_SERVICE)" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="EN_SERVICE">EN_SERVICE</SelectItem>
                                        <SelectItem value="EN_MAINTENANCE">EN_MAINTENANCE</SelectItem>
                                        <SelectItem value="HORS_SERVICE">HORS_SERVICE</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="hangarId">Hangar (UUID optionnel)</Label>
                            <Input
                                id="hangarId"
                                value={form.hangarId ?? ""}
                                onChange={(e) => setField("hangarId", e.target.value || null)}
                                placeholder="00000000-0000-0000-0000-000000000000"
                            />
                        </div>
                    </div>

                    <DialogFooter className="gap-2">
                        <Button variant="outline" onClick={() => setOpenCreate(false)}>Annuler</Button>
                        <Button
                            onClick={async () => {
                                try {
                                    await createAvion();
                                } catch (e: any) {
                                    alert(e?.message || "Erreur à la création");
                                }
                            }}
                        >
                            Enregistrer
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            {/* EDIT dialog */}
            <Dialog open={!!openEdit} onOpenChange={(open) => !open && setOpenEdit(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Modifier l’avion</DialogTitle>
                        <DialogDescription>Mettez à jour les informations et enregistrez.</DialogDescription>
                    </DialogHeader>

                    <div className="grid gap-4 py-2">
                        <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label>Immatriculation</Label>
                                <Input value={form.immatriculation} disabled />
                                <p className="text-xs text-muted-foreground">L’immatriculation n’est pas modifiable.</p>
                            </div>
                            <div className="space-y-2">
                                <Label htmlFor="type-edit">Type</Label>
                                <Input
                                    id="type-edit"
                                    value={form.type}
                                    onChange={(e) => setField("type", e.target.value)}
                                />
                            </div>
                        </div>

                        <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                            <div className="space-y-2">
                                <Label htmlFor="cap-edit">Capacité</Label>
                                <Input
                                    id="cap-edit"
                                    type="number"
                                    min={1}
                                    value={form.capacite}
                                    onChange={(e) => setField("capacite", e.target.value)}
                                />
                            </div>
                            <div className="space-y-2">
                                <Label>État</Label>
                                <Select value={form.etat ?? ""} onValueChange={(v) => setField("etat", v || undefined)}>
                                    <SelectTrigger>
                                        <SelectValue placeholder="(laisser tel quel)" />
                                    </SelectTrigger>
                                    <SelectContent>
                                        <SelectItem value="EN_SERVICE">EN_SERVICE</SelectItem>
                                        <SelectItem value="EN_MAINTENANCE">EN_MAINTENANCE</SelectItem>
                                        <SelectItem value="HORS_SERVICE">HORS_SERVICE</SelectItem>
                                    </SelectContent>
                                </Select>
                            </div>
                        </div>

                        <div className="space-y-2">
                            <Label htmlFor="hangar-edit">Hangar (UUID optionnel)</Label>
                            <Input
                                id="hangar-edit"
                                value={form.hangarId ?? ""}
                                onChange={(e) => setField("hangarId", e.target.value || null)}
                                placeholder="00000000-0000-0000-0000-000000000000"
                            />
                            <p className="text-xs text-muted-foreground">
                                Laisser vide pour ne pas changer. Utilisez “Retirer du hangar” dans le menu de la ligne pour détacher.
                            </p>
                        </div>
                    </div>

                    <DialogFooter className="gap-2">
                        <Button variant="outline" onClick={() => setOpenEdit(null)}>Annuler</Button>
                        <Button
                            onClick={async () => {
                                try {
                                    if (!openEdit) return;
                                    await updateAvion(openEdit.id);
                                } catch (e: any) {
                                    alert(e?.message || "Erreur à la mise à jour");
                                }
                            }}
                        >
                            Enregistrer
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>
        </div>
    );
}
