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
import { Alert, AlertDescription } from "@/components/ui/alert";
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
    Filter,
    ArrowUpDown,
    ArrowUp,
    ArrowDown,
    CheckCircle2,
    XCircle,
} from "lucide-react";

const API_BASE = process.env.NEXT_PUBLIC_API_BASE ?? "";

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

type SortConfig = {
    key: 'identifiant' | 'capacite' | null;
    direction: 'asc' | 'desc' | null;
};

type Notification = {
    type: 'success' | 'error';
    message: string;
} | null;

type ValidationErrors = {
    identifiant?: string;
    capacite?: string;
    etat?: string;
};

async function api<T>(path: string, init?: RequestInit): Promise<T> {
    const res = await fetch(`${API_BASE}${path}`, {
        ...init,
        headers: { "Content-Type": "application/json", ...(init?.headers || {}) },
        cache: "no-store",
    });
    if (!res.ok) {
        try {
            const errorJson = await res.json();
            throw new Error(errorJson.message || errorJson.error || "Erreur HTTP");
        } catch (parseError: any) {
            if (parseError instanceof Error && parseError.message !== "Erreur HTTP") {
                throw parseError;
            }
            throw new Error("Erreur HTTP");
        }
    }
    return res.json();
}

export default function HangarPage() {
    const [allItems, setAllItems] = useState<Hangar[]>([]);
    const [items, setItems] = useState<Hangar[]>([]);
    const [loading, setLoading] = useState(true);
    const [notification, setNotification] = useState<Notification>(null);

    const [sortConfig, setSortConfig] = useState<SortConfig>({ key: null, direction: null });
    const [etatFilter, setEtatFilter] = useState<string>("all");

    const [openCreate, setOpenCreate] = useState(false);
    const [openEdit, setOpenEdit] = useState<Hangar | null>(null);
    const [openViewAvions, setOpenViewAvions] = useState<Hangar | null>(null);
    const [avions, setAvions] = useState<Avion[]>([]);

    const [form, setForm] = useState({
        identifiant: "",
        capacite: 0,
        etat: "DISPONIBLE",
    });

    const [validationErrors, setValidationErrors] = useState<ValidationErrors>({});

    const identifiantRegex = /^[A-Z][A-Z0-9]{0,3}$/;

    const showNotification = (type: 'success' | 'error', message: string) => {
        setNotification({ type, message });
        setTimeout(() => setNotification(null), 5000);
    };

    const load = async () => {
        try {
            setLoading(true);
            const data = await api<Hangar[]>("/api/hangars");
            setAllItems(data);
            setItems(data);
        } catch (e: any) {
            showNotification('error', e.message);
        }
        setLoading(false);
    };

    useEffect(() => {
        load();
    }, []);

    useEffect(() => {
        let filtered = [...allItems];

        if (etatFilter !== "all") {
            filtered = filtered.filter(h => h.etat === etatFilter);
        }

        if (sortConfig.key && sortConfig.direction) {
            filtered.sort((a, b) => {
                let aValue: any;
                let bValue: any;

                if (sortConfig.key === 'capacite') {
                    aValue = a.capacite;
                    bValue = b.capacite;
                } else if (sortConfig.key === 'identifiant') {
                    aValue = a.identifiant;
                    bValue = b.identifiant;
                }

                if (typeof aValue === 'string') {
                    return sortConfig.direction === 'asc'
                        ? aValue.localeCompare(bValue)
                        : bValue.localeCompare(aValue);
                } else {
                    return sortConfig.direction === 'asc'
                        ? aValue - bValue
                        : bValue - aValue;
                }
            });
        }

        setItems(filtered);
    }, [allItems, sortConfig, etatFilter]);

    const handleSort = (key: 'identifiant' | 'capacite') => {
        setSortConfig(prev => {
            if (prev.key !== key) {
                return { key, direction: 'asc' };
            }
            if (prev.direction === 'asc') {
                return { key, direction: 'desc' };
            }
            if (prev.direction === 'desc') {
                return { key: null, direction: null };
            }
            return { key, direction: 'asc' };
        });
    };

    const getSortIcon = (key: 'identifiant' | 'capacite') => {
        if (sortConfig.key !== key || sortConfig.direction === null) {
            return <ArrowUpDown className="ml-2 h-4 w-4 inline-block" />;
        }
        return sortConfig.direction === 'asc'
            ? <ArrowUp className="ml-2 h-4 w-4 inline-block" />
            : <ArrowDown className="ml-2 h-4 w-4 inline-block" />;
    };

    const setField = (k: keyof typeof form, v: any) => {
        setForm((f) => ({ ...f, [k]: v }));
        if (validationErrors[k as keyof ValidationErrors]) {
            setValidationErrors((prev) => ({ ...prev, [k]: undefined }));
        }
    };

    const validateForm = (): boolean => {
        const errors: ValidationErrors = {};

        if (!form.identifiant.trim()) {
            errors.identifiant = "L'identifiant du hangar ne peut pas être vide.";
        } else if (!identifiantRegex.test(form.identifiant.toUpperCase())) {
            errors.identifiant = `Identifiant hangar '${form.identifiant}' invalide (ex: H1, H12, A3, B04).`;
        }

        if (Number(form.capacite) <= 0) {
            errors.capacite = "La capacité du hangar doit être > 0.";
        }

        setValidationErrors(errors);
        return Object.keys(errors).length === 0;
    };

    const startCreate = () => {
        setForm({ identifiant: "", capacite: 0, etat: "DISPONIBLE" });
        setValidationErrors({});
        setOpenCreate(true);
    };

    const startEdit = (h: Hangar) => {
        setForm({
            identifiant: h.identifiant,
            capacite: h.capacite,
            etat: h.etat === "PLEIN" ? "DISPONIBLE" : h.etat,
        });
        setValidationErrors({});
        setOpenEdit(h);
    };

    const createHangar = async () => {
        if (!validateForm()) {
            return;
        }

        try {
            const body = {
                identifiant: form.identifiant.toUpperCase(),
                capacite: Number(form.capacite),
                etat: form.etat,
            };
            await api("/api/hangars", {
                method: "POST",
                body: JSON.stringify(body),
            });
            setOpenCreate(false);
            setValidationErrors({});
            showNotification('success', 'Hangar créé avec succès');
            load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            // Check for duplicate identifiant
            if (errorMessage.includes('existe') ||
                errorMessage.includes('déjà') ||
                errorMessage.includes('already') ||
                errorMessage.includes('duplicate') ||
                errorMessage.includes('unique') ||
                errorMessage.includes('identifiant')) {
                errors.identifiant = e.message;
            }
            // Check for capacity errors
            else if (errorMessage.includes('capacité') || errorMessage.includes('capacite')) {
                errors.capacite = e.message;
            }
            // Check for état errors
            else if (errorMessage.includes('state') ||
                errorMessage.includes('état') ||
                errorMessage.includes('disponible') ||
                errorMessage.includes('maintenance')) {
                errors.etat = e.message;
            }
            // Unknown error type
            else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const updateHangar = async (id: string) => {
        try {
            const body = {
                capacite: Number(form.capacite),
                etat: form.etat,
            };
            await api(`/api/hangars/${id}`, {
                method: "PATCH",
                body: JSON.stringify(body),
            });

            setOpenEdit(null);
            setValidationErrors({});
            showNotification('success', 'Hangar modifié avec succès');
            load();
        } catch (e: any) {
            const errorMessage = e.message.toLowerCase();
            const errors: ValidationErrors = {};

            // Check for capacity-related errors
            if (errorMessage.includes('capacité') ||
                errorMessage.includes('capacite') ||
                errorMessage.includes('réduire')) {
                errors.capacite = e.message;
            }
            // Check for état/maintenance errors
            else if (errorMessage.includes('maintenance') ||
                errorMessage.includes('hangar') ||
                errorMessage.includes('avion') ||
                errorMessage.includes('cannot manually') ||
                errorMessage.includes('impossible') ||
                errorMessage.includes('disponible') ||
                errorMessage.includes('plein')) {
                errors.etat = e.message;
            }
            // Unknown error type
            else {
                showNotification('error', e.message);
                return;
            }

            setValidationErrors(errors);
        }
    };

    const deleteHangar = async (id: string) => {
        if (!confirm("Supprimer ce hangar ?")) return;
        try {
            await fetch(`${API_BASE}/api/hangars/${id}`, { method: "DELETE" });
            showNotification('success', 'Hangar supprimé avec succès');
            load();
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    const viewAvions = async (h: Hangar) => {
        setOpenViewAvions(h);
        try {
            const data = await api<Avion[]>(`/api/hangars/${h.id}/avions`);
            setAvions(data);
        } catch (e: any) {
            showNotification('error', e.message);
        }
    };

    const badgeForEtat = (etat: string) => {
        const e = etat.toUpperCase();
        if (e === "DISPONIBLE") return <Badge className="bg-green-600 text-white">{etat}</Badge>;
        if (e === "PLEIN") return <Badge className="bg-yellow-600 text-white">{etat}</Badge>;
        if (e === "MAINTENANCE") return <Badge className="bg-red-600 text-white">{etat}</Badge>;
        return <Badge>{etat}</Badge>;
    };

    return (
        <div className="mx-auto max-w-6xl px-4 py-8 space-y-6">
            <div className="flex items-center justify-between">
                <div>
                    <h1 className="text-2xl font-semibold">Hangars</h1>
                </div>
                <div className="flex items-center gap-2">
                    <Select value={etatFilter} onValueChange={setEtatFilter}>
                        <SelectTrigger className="w-[170px]">
                            <Filter className="mr-2 h-4 w-4" />
                            <SelectValue placeholder="Filtrer par état" />
                        </SelectTrigger>
                        <SelectContent>
                            <SelectItem value="all">Tous les états</SelectItem>
                            <SelectItem value="DISPONIBLE">DISPONIBLE</SelectItem>
                            <SelectItem value="PLEIN">PLEIN</SelectItem>
                            <SelectItem value="MAINTENANCE">MAINTENANCE</SelectItem>
                        </SelectContent>
                    </Select>
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

            {notification && (
                <Alert
                    className={
                        notification.type === 'success'
                            ? "border-green-300 bg-green-50"
                            : "border-red-300 bg-red-50"
                    }
                >
                    {notification.type === 'success' ? (
                        <CheckCircle2 className="h-4 w-4 text-green-600" />
                    ) : (
                        <XCircle className="h-4 w-4 text-red-600" />
                    )}
                    <AlertDescription
                        className={
                            notification.type === 'success'
                                ? "text-green-800"
                                : "text-red-800"
                        }
                    >
                        {notification.message}
                    </AlertDescription>
                </Alert>
            )}

            <Card className="overflow-hidden py-0">
                <div className="overflow-x-auto px-4 py-2">
                    <Table>
                        <TableHeader>
                            <TableRow>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('identifiant')}
                                >
                                    Identifiant
                                    {getSortIcon('identifiant')}
                                </TableHead>
                                <TableHead
                                    className="cursor-pointer select-none"
                                    onClick={() => handleSort('capacite')}
                                >
                                    Capacité
                                    {getSortIcon('capacite')}
                                </TableHead>
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
                                    <TableCell colSpan={4}>Aucun hangar</TableCell>
                                </TableRow>
                            ) : (
                                items.map((h) => (
                                    <TableRow key={h.id}>
                                        <TableCell>{h.identifiant}</TableCell>
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

            <Dialog open={openCreate} onOpenChange={setOpenCreate}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Nouveau hangar</DialogTitle>
                    </DialogHeader>
                    <div className="grid gap-4 py-2">
                        <div className="space-y-2">
                            <Label>Identifiant</Label>
                            <Input
                                placeholder="H1, A3, B04..."
                                value={form.identifiant}
                                onChange={(e) => setField("identifiant", e.target.value.toUpperCase())}
                                className={validationErrors.identifiant ? "border-red-500" : ""}
                            />
                            {validationErrors.identifiant && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.identifiant}
                                </p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label>Capacité</Label>
                            <Input
                                type="number"
                                min={1}
                                placeholder="10"
                                value={form.capacite}
                                onChange={(e) => setField("capacite", e.target.value)}
                                className={validationErrors.capacite ? "border-red-500" : ""}
                            />
                            {validationErrors.capacite && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.capacite}
                                </p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select value={form.etat} onValueChange={(v) => setField("etat", v)}>
                                <SelectTrigger className="w-full">
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="DISPONIBLE">DISPONIBLE</SelectItem>
                                    <SelectItem value="MAINTENANCE">MAINTENANCE</SelectItem>
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

            <Dialog open={!!openEdit} onOpenChange={(o) => {
                if (!o) {
                    setOpenEdit(null);
                    setValidationErrors({});
                }
            }}>
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
                                min={1}
                                value={form.capacite}
                                onChange={(e) => setField("capacite", e.target.value)}
                                className={validationErrors.capacite ? "border-red-500" : ""}
                            />
                            {validationErrors.capacite && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.capacite}
                                </p>
                            )}
                        </div>
                        <div className="space-y-2">
                            <Label>État</Label>
                            <Select
                                value={form.etat}
                                onValueChange={(v) => setField("etat", v)}
                            >
                                <SelectTrigger
                                    className={validationErrors.etat ? "border-red-500 w-full" : "w-full"}
                                >
                                    <SelectValue />
                                </SelectTrigger>
                                <SelectContent>
                                    <SelectItem value="DISPONIBLE">DISPONIBLE</SelectItem>
                                    <SelectItem value="MAINTENANCE">MAINTENANCE</SelectItem>
                                </SelectContent>
                            </Select>
                            {validationErrors.etat && (
                                <p className="text-sm text-red-600">
                                    {validationErrors.etat}
                                </p>
                            )}
                        </div>
                    </div>
                    <DialogFooter>
                        <Button variant="outline" onClick={() => {
                            setOpenEdit(null);
                            setValidationErrors({});
                        }}>
                            Annuler
                        </Button>
                        <Button onClick={() => openEdit && updateHangar(openEdit.id)}>
                            Enregistrer
                        </Button>
                    </DialogFooter>
                </DialogContent>
            </Dialog>

            <Dialog open={!!openViewAvions} onOpenChange={(o) => !o && setOpenViewAvions(null)}>
                <DialogContent>
                    <DialogHeader>
                        <DialogTitle>Avions dans le hangar {openViewAvions?.identifiant}</DialogTitle>
                    </DialogHeader>
                    {avions.length === 0 ? (
                        <p className="text-sm text-muted-foreground">Le Hangar est vide.</p>
                    ) : (
                        <ul className="space-y-2">
                            {avions.map((a) => (
                                <li key={a.id} className="flex items-center justify-between rounded-md border p-2">
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
