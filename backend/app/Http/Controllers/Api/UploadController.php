<?php

namespace App\Http\Controllers\Api;

use App\Http\Controllers\Controller;
use Illuminate\Http\JsonResponse;
use Illuminate\Http\Request;

class UploadController extends Controller
{
    public function upload(Request $request): JsonResponse
    {
        $request->validate([
            'file' => 'required|file|max:5120',
        ]);

        $path = $request->file('file')->store('uploads', 'public');

        return response()->json([
            'result' => true,
            'message' => 'Uploaded',
            'path' => asset("storage/{$path}"),
            'upload_id' => null,
        ]);
    }

    public function imageUpload(Request $request): JsonResponse
    {
        $request->validate([
            'image' => 'required|image|max:5120',
        ]);

        $path = $request->file('image')->store('uploads', 'public');

        return response()->json([
            'result' => true,
            'message' => 'Uploaded',
            'path' => asset("storage/{$path}"),
            'upload_id' => null,
        ]);
    }

    public function list(): JsonResponse
    {
        return response()->json([
            'data' => [],
            'links' => null,
            'meta' => null,
            'result' => true,
            'status' => 200,
        ]);
    }

    public function delete(int $id): JsonResponse
    {
        return response()->json([
            'result' => true,
            'message' => 'Deleted',
        ]);
    }
}
